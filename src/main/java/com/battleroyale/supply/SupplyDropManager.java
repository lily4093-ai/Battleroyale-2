package com.battleroyale.supply;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.*;
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
    
    private static final int DROPS_PER_ROUND = 100; // 1회당 100개
    private static final int DROP_INTERVAL = 20; // 1초 (20틱)
    
    public SupplyDropManager(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
        this.lootGenerator = new SupplyLootGenerator(plugin);
        this.dropCount = 0;
    }
    
    /**
     * 보급품 투하 시작
     * @param roundNumber 투하 회차 (1, 2, 3)
     */
    public void startSupplyDrop(int roundNumber) {
        if (supplyTask != null && !supplyTask.isCancelled()) {
            return; // 이미 진행 중
        }
        
        totalDropsThisRound = 0;
        
        Bukkit.broadcastMessage("§e§l[배틀로얄 2.0] §a제" + roundNumber + "차 보급품 투하가 시작됩니다!");
        Bukkit.broadcastMessage("§7100초 동안 100개의 보급 상자가 투하됩니다.");
        
        // 보스바 생성
        createSupplyBossBar();
        
        // 투하 시작
        supplyTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (totalDropsThisRound >= DROPS_PER_ROUND) {
                    // 투하 완료
                    finishSupplyDrop(roundNumber);
                    cancel();
                    return;
                }
                
                // 보급 상자 투하
                dropSupplyCrate();
                totalDropsThisRound++;
                
                // 보스바 업데이트
                updateSupplyBossBar();
            }
        }.runTaskTimer(plugin, 0L, DROP_INTERVAL);
    }
    
    /**
     * 보급 상자 투하
     */
    private void dropSupplyCrate() {
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        
        // 자기장 내 랜덤 위치
        Location dropLocation = findGroundLocation(world, border);
        
        if (dropLocation == null) {
            return; // 적절한 위치를 찾지 못함
        }
        
        // 상자 생성
        dropLocation.getBlock().setType(Material.CHEST);
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) dropLocation.getBlock().getState();
        
        // 루팅 생성
        Inventory inventory = chest.getInventory();
        lootGenerator.generateLoot(inventory);
        
        // 파티클 효과
        world.spawnParticle(Particle.FIREWORKS_SPARK, dropLocation.clone().add(0.5, 1, 0.5), 50, 0.5, 0.5, 0.5, 0.1);
        
        // 근처 플레이어에게 사운드
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(dropLocation) < 50) {
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1.0f);
            }
        }
    }
    
    /**
     * 지면 위치 찾기 (나뭇잎이나 공중이 아닌 실제 땅)
     */
    private Location findGroundLocation(World world, WorldBorder border) {
        Random random = new Random();
        double size = border.getSize() / 2;
        
        for (int attempts = 0; attempts < 10; attempts++) {
            double x = (random.nextDouble() * size * 2) - size;
            double z = (random.nextDouble() * size * 2) - size;
            
            Location loc = world.getHighestBlockAt((int) x, (int) z).getLocation();
            Material blockType = loc.getBlock().getType();
            
            // 나뭇잎이나 불안정한 블록 제외
            if (blockType != Material.AIR && 
                blockType != Material.WATER && 
                blockType != Material.LAVA &&
                !blockType.name().contains("LEAVES")) {
                return loc.add(0, 1, 0); // 블록 위에 배치
            }
        }
        
        // 실패 시 기본 위치
        double x = (random.nextDouble() * size * 2) - size;
        double z = (random.nextDouble() * size * 2) - size;
        return world.getHighestBlockAt((int) x, (int) z).getLocation().add(0, 1, 0);
    }
    
    /**
     * 보급품 보스바 생성
     */
    private void createSupplyBossBar() {
        if (supplyBossBar != null) {
            supplyBossBar.removeAll();
        }
        
        supplyBossBar = Bukkit.createBossBar(
                "§a§l보급품 투하 진행 중...",
                BarColor.GREEN,
                BarStyle.SEGMENTED_10
        );
        
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
        if (supplyBossBar == null) return;
        
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
