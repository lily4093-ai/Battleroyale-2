package com.battleroyale.supply;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

/**
 * 보급품 투하 시스템 관리
 * - 게임 시작 직후 1회, 이후 5분마다 총 3회 투하
 * - 1회당 100초 동안 1초에 1개씩 총 100개 투하
 */
public class SupplyDropManager {

    private final BattleRoyalePlugin plugin;
    private final SupplyLootGenerator lootGenerator;
    private BossBar supplyBossBar;
    private BukkitTask supplyTask;
    private BukkitTask compassTask;
    private int totalDropsThisRound;
    private int completedDropsThisRound;
    private int currentRoundNumber;

    // 보급품 위치 추적
    private final Set<Location> supplyCrateLocations = new HashSet<>();
    private final Set<Location> openedSupplyCrates = new HashSet<>();

    private static final int DROPS_PER_ROUND = 100; // 1회당 100개
    private static final int DROP_INTERVAL = 60; // 3초 (60틱) - 3초마다 2개

    public SupplyDropManager(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
        this.lootGenerator = new SupplyLootGenerator(plugin);
    }

    /**
     * 보급품 투하 시작
     * 
     * @param roundNumber 투하 회차 (1, 2, 3)
     */
    public void startSupplyDrop(int roundNumber) {
        if (supplyTask != null && !supplyTask.isCancelled()) {
            return; // 이미 진행 중
        }

        totalDropsThisRound = 0;
        completedDropsThisRound = 0;
        currentRoundNumber = roundNumber;

        broadcast("§e§l[배틀로얄 2.0] §a제" + roundNumber + "차 보급품 투하가 시작됩니다!");
        broadcast("§73초마다 2개의 보급 상자가 투하됩니다.");

        createSupplyBossBar();

        // 즉시 투하 루프 시작 (3초마다 2개)
        supplyTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (totalDropsThisRound >= DROPS_PER_ROUND) {
                    cancel();
                    return;
                }

                // 3초마다 2개씩 투하
                processSupplyDrop();
                totalDropsThisRound++;

                if (totalDropsThisRound < DROPS_PER_ROUND) {
                    processSupplyDrop();
                    totalDropsThisRound++;
                }
            }
        }.runTaskTimer(plugin, 0L, DROP_INTERVAL);
    }

    /**
     * 보급품 투하 처리
     */
    private void processSupplyDrop() {
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        Random random = new Random();
        double size = border.getSize() / 2;

        double x = (random.nextDouble() * size * 2) - size;
        double z = (random.nextDouble() * size * 2) - size;

        // 청크 로드 확인 및 처리
        int chunkX = (int) x >> 4;
        int chunkZ = (int) z >> 4;

        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            world.loadChunk(chunkX, chunkZ);
        }

        // 보급품 투하 위치 탐색
        Location loc = findGroundLocationAt(world, x, z);
        if (loc != null) {
            dropSupplyCrateAt(loc);
            completedDropsThisRound++;
            updateSupplyBossBar();

            // 모든 투하가 완료되었는지 체크
            if (completedDropsThisRound >= DROPS_PER_ROUND) {
                finishSupplyDrop(currentRoundNumber);
            }
        }
    }

    /**
     * 특정 X, Z 좌표에서 지면 위치 찾기
     */
    private Location findGroundLocationAt(World world, double x, double z) {
        for (int y = world.getMaxHeight() - 1; y > 60; y--) {
            Block block = world.getBlockAt((int) x, y, (int) z);
            Material type = block.getType();

            if (type != Material.AIR && type != Material.WATER && type != Material.LAVA &&
                    !type.name().contains("LEAVES")) {
                return block.getLocation().add(0, 1, 0);
            }
        }
        return world.getHighestBlockAt((int) x, (int) z).getLocation().add(0, 1, 0);
    }

    /**
     * 지정된 위치에 보급 상자 투하
     */
    private void dropSupplyCrateAt(Location dropLocation) {
        // 상자 생성
        dropLocation.getBlock().setType(Material.CHEST);
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) dropLocation.getBlock().getState();

        // 루팅 생성
        Inventory inventory = chest.getInventory();
        lootGenerator.generateLoot(inventory);

        // 보급품 위치 추적
        Location blockLoc = dropLocation.getBlock().getLocation();
        supplyCrateLocations.add(blockLoc);
    }

    /**
     * 보금품 보스바 생성
     */
    private void createSupplyBossBar() {
        if (supplyBossBar != null) {
            supplyBossBar.removeAll();
        }

        supplyBossBar = Bukkit.createBossBar(
                "§a§l보급품 투하 진행 중...",
                BarColor.GREEN,
                BarStyle.SEGMENTED_10);

        for (Player player : Bukkit.getOnlinePlayers()) {
            supplyBossBar.addPlayer(player);
        }

        supplyBossBar.setProgress(0.0);
        supplyBossBar.setVisible(true);
    }

    /**
     * 보급품 보스바 업데이트
     */
    private void updateSupplyBossBar() {
        if (supplyBossBar == null)
            return;

        double progress = (double) completedDropsThisRound / DROPS_PER_ROUND;
        supplyBossBar.setProgress(Math.min(1.0, progress));
        supplyBossBar.setTitle("§a§l보급품 투하 §f| §e" + completedDropsThisRound + "§f개 투하 완료됨");
    }

    /**
     * 보급품 투하 완료
     */
    private void finishSupplyDrop(int roundNumber) {
        broadcast("§e§l[배틀로얄 2.0] §a제" + roundNumber + "차 보급품 투하가 완료되었습니다!");

        if (supplyBossBar != null) {
            supplyBossBar.setVisible(false);
            supplyBossBar.removeAll();
            supplyBossBar = null;
        }

        // 완료 사운드
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    /**
     * 보급품 시스템 정리
     */
    public void cleanup() {
        if (supplyTask != null) {
            supplyTask.cancel();
        }

        if (compassTask != null) {
            compassTask.cancel();
        }

        if (supplyBossBar != null) {
            supplyBossBar.removeAll();
            supplyBossBar = null;
        }

        supplyCrateLocations.clear();
        openedSupplyCrates.clear();
    }

    /**
     * 보급 상자가 열렸음을 기록
     */
    public void markSupplyOpened(Location location) {
        Location blockLoc = location.getBlock().getLocation();
        if (supplyCrateLocations.contains(blockLoc)) {
            openedSupplyCrates.add(blockLoc);
            // supplyCrateLocations.remove(blockLoc); // cleanupDestroyedCrates에서 처리하도록 둠
        }
    }

    /**
     * 특정 위치가 보급 상자인지 확인
     */
    public boolean isSupplyCrate(Location location) {
        return supplyCrateLocations.contains(location.getBlock().getLocation());
    }

    /**
     * 플레이어에게 가장 가까운 열리지 않은 보급품 위치 반환
     */
    public Location getNearestUnopenedSupply(Player player) {
        Location playerLoc = player.getLocation();
        Location nearest = null;
        double nearestDist = Double.MAX_VALUE;

        // 월드보더 정보 가져오기
        World world = playerLoc.getWorld();
        WorldBorder border = world.getWorldBorder();
        Location borderCenter = border.getCenter();
        double borderRadius = border.getSize() / 2.0;

        for (Location loc : supplyCrateLocations) {
            // 이미 열린/파괴된 보급품은 제외
            if (openedSupplyCrates.contains(loc)) {
                continue;
            }

            // 같은 월드인지 확인
            if (!loc.getWorld().equals(playerLoc.getWorld())) {
                continue;
            }

            // 월드보더 밖의 보급품은 제외
            double dx = loc.getX() - borderCenter.getX();
            double dz = loc.getZ() - borderCenter.getZ();
            double distFromCenter = Math.sqrt(dx * dx + dz * dz);

            if (distFromCenter > borderRadius) {
                continue; // 월드보더 밖
            }

            // 플레이어와의 거리 계산 (X, Z만 고려, Y는 무시)
            dx = loc.getX() - playerLoc.getX();
            dz = loc.getZ() - playerLoc.getZ();
            double dist = dx * dx + dz * dz; // distanceSquared와 동일하지만 Y 제외

            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = loc;
            }
        }

        return nearest;
    }

    /**
     * 나침반 업데이트 시작
     */
    public void startCompassUpdater() {
        if (compassTask != null && !compassTask.isCancelled()) {
            return;
        }

        compassTask = new BukkitRunnable() {
            private int cleanupCounter = 0;

            @Override
            public void run() {
                // 파괴된 상자 정리 - 매 100틱(5초)마다만 수행하여 부하 감소
                cleanupCounter++;
                if (cleanupCounter >= 20) { // 5틱 * 20 = 100틱
                    cleanupDestroyedCrates();
                    cleanupCounter = 0;
                }

                if (supplyCrateLocations.isEmpty()) {
                    // 모든 보급 상자가 사라진 경우 나침반 초기화
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getGameMode() == GameMode.SURVIVAL) {
                            player.setCompassTarget(player.getWorld().getSpawnLocation());
                        }
                    }
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() != GameMode.SURVIVAL) {
                        continue;
                    }

                    Location nearest = getNearestUnopenedSupply(player);
                    if (nearest != null) {
                        player.setCompassTarget(nearest);
                    } else {
                        // 더 이상 열지 않은 보급품이 없으면 스폰 지점으로 초기화
                        player.setCompassTarget(player.getWorld().getSpawnLocation());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    /**
     * 파괴된 상자들을 정리
     */
    private void cleanupDestroyedCrates() {
        if (supplyCrateLocations.isEmpty())
            return;

        Iterator<Location> iterator = supplyCrateLocations.iterator();
        while (iterator.hasNext()) {
            Location loc = iterator.next();
            if (loc.getWorld() == null)
                continue;

            // 이미 열린 것으로 표시된 경우 목록에서 제거
            if (openedSupplyCrates.contains(loc)) {
                iterator.remove();
                openedSupplyCrates.remove(loc); // 메모리 관리: 추적 목록에서도 제거
                continue;
            }

            // 청크가 로드된 경우에만 블록 확인
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;

            if (loc.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                Material type = loc.getBlock().getType();
                if (type != Material.CHEST) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 나침반 업데이트 중지
     */
    public void stopCompassUpdater() {
        if (compassTask != null) {
            compassTask.cancel();
            compassTask = null;
        }
    }

    private void broadcast(String message) {
        Bukkit.broadcast(LegacyComponentSerializer.legacySection().deserialize(message));
    }
}
