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
import java.util.*;
import java.util.stream.Collectors;

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
    private int totalDrops;

    // 보급품 위치 추적 (좌표 문자열 사용: "world:x:y:z")
    private final Set<String> supplyCrateLocations = new HashSet<>();
    private final Set<String> openedSupplyCrates = new HashSet<>();
    private final Set<String> pendingSupplyLocations = new HashSet<>();
    private final Map<String, Location> locationMap = new HashMap<>(); // 키 -> 로케이션 매핑

    public SupplyDropManager(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
        this.lootGenerator = new SupplyLootGenerator(plugin);
    }

    /**
     * 보급품 상시 투하 시작
     * 2초마다 1개씩 게임 종료 시까지 투하됩니다.
     */
    public void startContinuousSupplyDrop() {
        if (supplyTask != null && !supplyTask.isCancelled()) {
            return; // 이미 진행 중
        }

        totalDrops = 0;

        broadcast("§e§l[배틀로얄 2.0] §a보급품 투하 시스템이 가동되었습니다!");
        broadcast("§7게임 내내 2초마다 1개의 보급 상자가 무작위 위치에 투하됩니다.");

        createSupplyBossBar();

        // 인원수에 따른 투하 간격 조정 태스크 (1초마다 체크)
        supplyTask = new BukkitRunnable() {
            private int ticksUntilNextDrop = 0;

            @Override
            public void run() {
                List<Player> survivalPlayers = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                        .collect(Collectors.toList());

                if (survivalPlayers.isEmpty()) {
                    // Bukkit.getLogger().info("[BattleRoyale] 생존 플레이어가 없어 보급 투하를 대기합니다.");
                    return;
                }

                if (ticksUntilNextDrop <= 0) {
                    try {
                        Bukkit.getLogger().info("[BattleRoyale] 보급 투하 시도... (현재 누적: " + totalDrops + ", 플레이어 수: "
                                + survivalPlayers.size() + ")");
                        processSupplyDrop();
                    } catch (Exception e) {
                        Bukkit.getLogger().severe("[BattleRoyale] 보급 투하 중 오류 발생: " + e.getMessage());
                        e.printStackTrace();
                    }

                    // 다음 투하 시간 계산 (1인: 10초, 2인: 5초, 3인 이상: 3초)
                    int playerCount = survivalPlayers.size();
                    if (playerCount <= 1) {
                        ticksUntilNextDrop = 10; // 10초
                    } else if (playerCount == 2) {
                        ticksUntilNextDrop = 5; // 5초
                    } else {
                        ticksUntilNextDrop = 3; // 3초
                    }
                } else {
                    ticksUntilNextDrop--;
                }
            }
        }.runTaskTimer(plugin, 40L, 20L); // 1초(20틱) 주기
    }

    /**
     * 보급품 투하 처리
     */
    private void processSupplyDrop() {
        World world = Bukkit.getWorlds().get(0);
        if (world == null)
            return;

        WorldBorder border = world.getWorldBorder();
        Random random = new Random();
        double borderSize = border.getSize() / 2;
        Location center = border.getCenter();

        double x, z;
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                .collect(Collectors.toList());

        // 70% 확률로 유저 근처에, 30% 확률로 완전 랜덤하게 투하
        if (!players.isEmpty() && random.nextDouble() < 0.7) {
            Player target = players.get(random.nextInt(players.size()));
            Location pLoc = target.getLocation();

            // 유저 기준 50~150블럭 사이의 무작위 위치
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 50 + (random.nextDouble() * 100);

            x = pLoc.getX() + (Math.cos(angle) * radius);
            z = pLoc.getZ() + (Math.sin(angle) * radius);

            // 보더 밖으로 나가지 않도록 보정
            x = Math.max(center.getX() - borderSize, Math.min(center.getX() + borderSize, x));
            z = Math.max(center.getZ() - borderSize, Math.min(center.getZ() + borderSize, z));
        } else {
            // 자기장 내의 완전 무작위 좌표
            x = center.getX() + (random.nextDouble() * borderSize * 2) - borderSize;
            z = center.getZ() + (random.nextDouble() * borderSize * 2) - borderSize;
        }

        // 청크 로드 확인 및 처리 (가상화되어 있으므로 로드할 필요는 없지만 위치 탐색용)
        int chunkX = (int) x >> 4;
        int chunkZ = (int) z >> 4;

        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            world.loadChunk(chunkX, chunkZ);
        }

        // 보급품 투하 위치 탐색
        Location loc = findGroundLocationAt(world, x, z);
        if (loc != null) {
            // 즉시 설치 대신 가상 위치로 등록
            Location blockLoc = loc.getBlock().getLocation();
            String key = locToKey(blockLoc);
            pendingSupplyLocations.add(key);
            locationMap.put(key, blockLoc);
            totalDrops++;
            updateSupplyBossBar();

            // 만약 해당 지역이 이미 로드되어 있다면 즉시 설치 시도
            if (world.isChunkLoaded(blockLoc.getBlockX() >> 4, blockLoc.getBlockZ() >> 4)) {
                checkAndRealizePendingDrop(blockLoc);
            }

            Bukkit.getLogger().info("[BattleRoyale] 보급 투하 예약 완료: " + key + " (누적: " + totalDrops + ")");
        } else {
            Bukkit.getLogger().warning("[BattleRoyale] 보급 투하 위치를 찾지 못했습니다 (x=" + x + ", z=" + z + ")");
        }
    }

    /**
     * 가상 보급품을 실제 블록으로 설치 (지연 설치)
     */
    public void checkAndRealizePendingDrop(Location loc) {
        String key = locToKey(loc);
        if (!pendingSupplyLocations.contains(key) || supplyCrateLocations.contains(key)) {
            return;
        }

        // 실제 설치
        loc.getBlock().setType(Material.CHEST);
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) loc.getBlock().getState();

        // 루팅 생성 (이때 계산)
        lootGenerator.generateLoot(chest.getInventory());

        // 추적 리스트 이동
        pendingSupplyLocations.remove(key);
        supplyCrateLocations.add(key);
    }

    private String locToKey(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return "unknown";
        return loc.getWorld().getName().toLowerCase() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":"
                + loc.getBlockZ();
    }

    /**
     * 청크 내의 모든 대기 중인 보급품 설치
     */
    public void realizePendingDropsInChunk(Chunk chunk) {
        if (pendingSupplyLocations.isEmpty())
            return;

        List<String> toRealizeKeys = new ArrayList<>();
        for (String key : pendingSupplyLocations) {
            Location loc = locationMap.get(key);
            if (loc != null && loc.getBlockX() >> 4 == chunk.getX() && loc.getBlockZ() >> 4 == chunk.getZ()) {
                toRealizeKeys.add(key);
            }
        }

        for (String key : toRealizeKeys) {
            Location loc = locationMap.get(key);
            if (loc != null) {
                checkAndRealizePendingDrop(loc);
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
    public void dropSupplyCrateAt(Location dropLocation) {
        // 상자 생성
        dropLocation.getBlock().setType(Material.CHEST);
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) dropLocation.getBlock().getState();

        // 루팅 생성
        Inventory inventory = chest.getInventory();
        lootGenerator.generateLoot(inventory);

        // 보급품 위치 추적
        Location blockLoc = dropLocation.getBlock().getLocation();
        String key = locToKey(blockLoc);
        supplyCrateLocations.add(key);
        locationMap.put(key, blockLoc);
    }

    /**
     * 보금품 보스바 생성
     */
    private void createSupplyBossBar() {
        if (supplyBossBar != null) {
            supplyBossBar.removeAll();
        }

        supplyBossBar = Bukkit.createBossBar(
                "§a§l보급품 투하 활성화",
                BarColor.GREEN,
                BarStyle.SOLID);

        for (Player player : Bukkit.getOnlinePlayers()) {
            supplyBossBar.addPlayer(player);
        }

        supplyBossBar.setProgress(1.0);
        supplyBossBar.setVisible(true);
    }

    /**
     * 보급품 보스바 업데이트
     */
    private void updateSupplyBossBar() {
        if (supplyBossBar == null)
            return;

        supplyBossBar.setTitle("§a§l보급품 투하 §f| §e누적 " + totalDrops + "개 투하됨");
    }

    /**
     * 보급품 투하 완료
     * (현재 상시 투하 방식으로 변경되어 사용되지 않음)
     */
    public void stopSupplyDrop() {
        if (supplyTask != null) {
            supplyTask.cancel();
            supplyTask = null;
        }

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
        pendingSupplyLocations.clear();
    }

    /**
     * 보급 상자가 열렸음을 기록
     */
    public void markSupplyOpened(Location location) {
        String key = locToKey(location);
        if (supplyCrateLocations.contains(key) || pendingSupplyLocations.contains(key)) {
            openedSupplyCrates.add(key);
            // 나침반에서 더 이상 나오지 않게 함
        }
    }

    /**
     * 보급 상자가 파괴되었을 때 처리
     */
    public void markSupplyBroken(Location location) {
        String key = locToKey(location);
        supplyCrateLocations.remove(key);
        pendingSupplyLocations.remove(key);
        openedSupplyCrates.add(key); // 아예 제외되도록 열린 상자 목록에 추가
    }

    /**
     * 특정 위치가 보급 상자인지 확인
     */
    public boolean isSupplyCrate(Location location) {
        String key = locToKey(location);
        return supplyCrateLocations.contains(key) || pendingSupplyLocations.contains(key);
    }

    /**
     * 플레이어에게 가장 가까운 열리지 않은 보급품 위치 반환
     */
    public Location getNearestUnopenedSupply(Location playerLoc) {
        Location nearest = null;
        double nearestDist = Double.MAX_VALUE;

        World world = playerLoc.getWorld();
        WorldBorder border = world.getWorldBorder();
        Location borderCenter = border.getCenter();

        // 모든 보급 위치 (설치된 것 + 대기 중인 것)
        List<String> allKeys = new ArrayList<>(supplyCrateLocations);
        allKeys.addAll(pendingSupplyLocations);

        for (String key : allKeys) {
            // 이미 열린/파괴된 보급품은 제외
            if (openedSupplyCrates.contains(key)) {
                continue;
            }

            Location loc = locationMap.get(key);
            if (loc == null)
                continue;

            // 실시간 상태 확인: 만약 블록이 로드된 상태인데 상자가 아니라면 이미 파괴/열린 것임 (대기 중인 보급품 제외)
            if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                Material type = loc.getBlock().getType();
                if (type != Material.CHEST && type != Material.TRAPPED_CHEST && !pendingSupplyLocations.contains(key)) {
                    // 즉시 정리
                    openedSupplyCrates.add(key);
                    supplyCrateLocations.remove(key);
                    continue;
                }
            }

            // 같은 월드인지 확인 (대기 중인 보급품도 위치 데이터는 있으므로 체크 가능)
            if (!loc.getWorld().getName().equalsIgnoreCase(playerLoc.getWorld().getName())) {
                continue;
            }

            // 월드보더 밖의 보급품은 제외 (정사각형 보더 체크)
            double borderRadius = border.getSize() / 2.0;
            double dx = loc.getX() - borderCenter.getX();
            double dz = loc.getZ() - borderCenter.getZ();

            if (Math.abs(dx) > borderRadius || Math.abs(dz) > borderRadius) {
                continue; // 월드보더 밖
            }

            // 플레이어와의 거리 계산 (X, Z만 고려, Y는 무시)
            double pdx = loc.getX() - playerLoc.getX();
            double pdz = loc.getZ() - playerLoc.getZ();
            double distSq = pdx * pdx + pdz * pdz;

            if (distSq < nearestDist) {
                nearestDist = distSq;
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
            @Override
            public void run() {
                // 모든 보급 위치 (설치된 것 + 대기 중인 것)
                List<String> allKeys = new ArrayList<>(supplyCrateLocations);
                allKeys.addAll(pendingSupplyLocations);

                if (allKeys.isEmpty()) {
                    // 모든 보급 상자가 없는 경우 나침반 초기화
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getGameMode() == GameMode.SURVIVAL) {
                            player.setCompassTarget(player.getWorld().getSpawnLocation());
                            Bukkit.getLogger().info("[BattleRoyale] " + player.getName() + "의 나침반 목표: 스폰 지점 (보급품 없음)");
                        }
                    }
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() != GameMode.SURVIVAL) {
                        continue;
                    }

                    Location nearest = getNearestUnopenedSupply(player.getLocation());
                    if (nearest != null) {
                        player.setCompassTarget(nearest);
                        Bukkit.getLogger().info(
                                "[BattleRoyale] " + player.getName() + "의 나침반 목표: 보급품 (" + locToKey(nearest) + ")");
                    } else {
                        player.setCompassTarget(player.getWorld().getSpawnLocation());
                        Bukkit.getLogger().info("[BattleRoyale] " + player.getName() + "의 나침반 목표: 스폰 지점 (보급품 없음)");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    /**
     * 파괴된 상자들을 정리
     */
    public void cleanupDestroyedCrates() {
        Iterator<String> it = supplyCrateLocations.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Location loc = locationMap.get(key);
            if (loc == null) { // Location might be null if it was never added to locationMap or removed
                it.remove();
                openedSupplyCrates.add(key);
                continue;
            }

            // 이미 열린 것으로 표시된 경우 목록에서 제거
            if (openedSupplyCrates.contains(key)) {
                it.remove();
                // openedSupplyCrates.remove(key); // Keep it in openedSupplyCrates to prevent
                // re-adding
                continue;
            }

            // 청크가 로드된 경우에만 블록 확인
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;

            if (loc.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                Material type = loc.getBlock().getType();
                if (type != Material.CHEST) {
                    it.remove();
                    openedSupplyCrates.add(key);
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
        Bukkit.broadcastMessage(message);
    }
}
