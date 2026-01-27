package com.battleroyale.supply;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    private int dropCount;
    private int totalDropsThisRound;
    private int completedDropsThisRound;

    private static final int DROPS_PER_ROUND = 100; // 1회당 100개
    private static final int DROP_INTERVAL = 2; // 0.1초 (2틱) - 초당 10개

    public SupplyDropManager(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
        this.lootGenerator = new SupplyLootGenerator(plugin);
        this.dropCount = 0;
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

        Bukkit.broadcastMessage("§e§l[배틀로얄 2.0] §a제" + roundNumber + "차 보급품 투하가 시작됩니다!");
        Bukkit.broadcastMessage("§7초당 10개의 보급 상자가 투하됩니다.");

        createSupplyBossBar();

        // 즉시 투하 루프 시작
        supplyTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (totalDropsThisRound >= DROPS_PER_ROUND) {
                    cancel();
                    return;
                }

                // 비동기로 위치 찾기 및 투하
                scheduleAsyncDrop();
                totalDropsThisRound++;
            }
        }.runTaskTimer(plugin, 0L, DROP_INTERVAL);
    }

    /**
     * 비동기로 위치를 찾고 보급품을 투하함
     */
    private void scheduleAsyncDrop() {
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        Random random = new Random();
        double size = border.getSize() / 2;

        double x = (random.nextDouble() * size * 2) - size;
        double z = (random.nextDouble() * size * 2) - size;

        // Paper/Spigot의 비동기 청크 로딩 사용 (서버 랙 방지)
        world.getChunkAtAsync((int) x >> 4, (int) z >> 4).thenAccept(chunk -> {
            // 메인 스레드에서 블록 설치
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Location loc = findGroundLocationAt(world, x, z);
                if (loc != null) {
                    dropSupplyCrateAt(loc);
                    completedDropsThisRound++;
                    updateSupplyBossBar();

                    // 모든 투하가 완료되었는지 체크
                    if (completedDropsThisRound >= DROPS_PER_ROUND) {
                        finishSupplyDrop(1); // 라운드 번호는 적절히 처리 필요
                    }
                }
            });
        });
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

        // 파티클 효과
        dropLocation.getWorld().spawnParticle(
                Particle.FLAME,
                dropLocation.clone().add(0.5, 1, 0.5),
                50, 0.5, 0.5, 0.5, 0.1);

        dropCount++;
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

        double progress = (double) totalDropsThisRound / DROPS_PER_ROUND;
        supplyBossBar.setProgress(Math.min(1.0, progress));
        supplyBossBar.setTitle("§a§l보급품 투하: §f" + totalDropsThisRound + " / " + DROPS_PER_ROUND);
    }

    /**
     * 보급품 투하 완료
     */
    private void finishSupplyDrop(int roundNumber) {
        Bukkit.broadcastMessage("§e§l[배틀로얄 2.0] §a제" + roundNumber + "차 보급품 투하가 완료되었습니다!");

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

        if (supplyBossBar != null) {
            supplyBossBar.removeAll();
            supplyBossBar = null;
        }
    }
}
