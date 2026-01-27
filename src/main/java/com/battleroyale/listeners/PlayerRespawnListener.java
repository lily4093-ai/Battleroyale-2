package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.game.GameManager;
import com.battleroyale.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * 플레이어 리스폰 이벤트 처리
 */
public class PlayerRespawnListener implements Listener {
    
    private final BattleRoyalePlugin plugin;
    
    public PlayerRespawnListener(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        GameManager gameManager = plugin.getGameManager();
        
        // 게임이 진행 중이 아니면 기본 스폰
        if (gameManager.getGameState() != GameState.ACTIVE && 
            gameManager.getGameState() != GameState.DEATH_TIME) {
            return;
        }
        
        // 리스폰 위치는 GameManager의 scheduleRespawn에서 처리됨
        // 여기서는 추가 설정만 수행
    }
}
