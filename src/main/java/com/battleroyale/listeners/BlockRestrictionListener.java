package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 블럭 설치/파괴 제한 리스너
 * - 땅을 파고 들어가는 행위 금지
 * - 블럭을 쌓고 높이 올라가는 행위 금지
 * - 임시 엄폐용 블럭만 허용
 */
public class BlockRestrictionListener implements Listener {
    
    private final BattleRoyalePlugin plugin;
    
    // 플레이어가 설치한 블럭 추적 (엄폐용 블럭만 파괴 가능)
    private final Map<Location, UUID> placedBlocks = new HashMap<>();
    
    // 플레이어별 최근 설치 높이 추적
    private final Map<UUID, Integer> playerPlaceHeight = new HashMap<>();
    
    public BlockRestrictionListener(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        
        // 게임 진행 중이 아니면 허용
        GameState state = plugin.getGameManager().getGameState();
        if (state != GameState.ACTIVE && state != GameState.DEATH_TIME) {
            return;
        }
        
        // 보급 상자는 제외
        if (block.getType() == Material.CHEST) {
            return;
        }
        
        Location blockLoc = block.getLocation();
        Location playerLoc = player.getLocation();
        
        // 1. 땅 아래로 파고 들어가는지 체크 (플레이어 발 아래 블럭 설치 금지)
        if (blockLoc.getBlockY() < playerLoc.getBlockY() - 1) {
            event.setCancelled(true);
            player.sendMessage("§c[배틀로얄 2.0] 땅을 파고 들어가는 행위는 금지됩니다!");
            return;
        }
        
        // 2. 높이 쌓기 제한 (연속으로 3블럭 이상 위로 쌓기 금지)
        Integer lastHeight = playerPlaceHeight.get(player.getUniqueId());
        int currentHeight = blockLoc.getBlockY();
        
        if (lastHeight != null) {
            int heightDiff = currentHeight - lastHeight;
            
            // 연속으로 2블럭 이상 위로 쌓으면 금지
            if (heightDiff >= 2) {
                event.setCancelled(true);
                player.sendMessage("§c[배틀로얄 2.0] 블럭을 높이 쌓아 올라가는 행위는 금지됩니다!");
                player.sendMessage("§7블럭은 임시 엄폐용으로만 사용하세요!");
                return;
            }
        }
        
        // 3. 플레이어 머리 위 블럭 설치 금지 (타워링 방지)
        if (blockLoc.getBlockY() > playerLoc.getBlockY() + 2) {
            event.setCancelled(true);
            player.sendMessage("§c[배틀로얄 2.0] 너무 높은 위치에 블럭을 설치할 수 없습니다!");
            return;
        }
        
        // 허용된 블럭 설치 - 추적에 추가
        placedBlocks.put(blockLoc, player.getUniqueId());
        playerPlaceHeight.put(player.getUniqueId(), currentHeight);
        
        // 5초 후 높이 추적 초기화 (연속 쌓기가 아니면 허용)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            playerPlaceHeight.remove(player.getUniqueId());
        }, 100L);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // 게임 진행 중이 아니면 허용
        GameState state = plugin.getGameManager().getGameState();
        if (state != GameState.ACTIVE && state != GameState.DEATH_TIME) {
            return;
        }
        
        // 보급 상자는 파괴 가능
        if (block.getType() == Material.CHEST) {
            return;
        }
        
        // 잔디, 나뭇잎, 잔디 블록 등 자연물 파괴 허용
        Material type = block.getType();
        String typeName = type.name();
        if (typeName.contains("GRASS") || typeName.contains("LEAVES") || 
            typeName.contains("FLOWER") || typeName.contains("FERN") ||
            type == Material.VINE || type == Material.GLOW_LICHEN || 
            type == Material.SNOW) {
            return;
        }
        
        Location blockLoc = block.getLocation();
        
        // 플레이어가 설치한 블럭만 파괴 가능 (엄폐용 블럭)
        if (!placedBlocks.containsKey(blockLoc)) {
            // 자연 생성된 블럭 파괴 금지
            event.setCancelled(true);
            player.sendMessage("§c[배틀로얄 2.0] 자연 생성된 블럭은 파괴할 수 없습니다!");
            player.sendMessage("§7엄폐용으로 설치한 블럭만 파괴 가능합니다!");
            return;
        }
        
        // 파괴 성공 시 추적에서 제거
        placedBlocks.remove(blockLoc);
    }
    
    /**
     * 게임 종료 시 추적 데이터 초기화
     */
    public void clearTracking() {
        placedBlocks.clear();
        playerPlaceHeight.clear();
    }
}
