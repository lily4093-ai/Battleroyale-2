package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.game.GameManager;
import com.battleroyale.game.GameState;
import com.battleroyale.game.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * 플레이어 사망 이벤트 처리
 */
public class PlayerDeathListener implements Listener {
    
    private final BattleRoyalePlugin plugin;
    
    public PlayerDeathListener(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        GameManager gameManager = plugin.getGameManager();
        
        // 게임이 진행 중일 때만 처리
        if (gameManager.getGameState() != GameState.ACTIVE && 
            gameManager.getGameState() != GameState.DEATH_TIME) {
            return;
        }
        
        // 사망 메시지 커스터마이징
        event.setDeathMessage(null);
        
        // 사망 처리
        gameManager.handlePlayerDeath(victim, killer);
        
        // 드롭 아이템 제거 (인벤토리는 리스폰 시 절반 삭제)
        event.getDrops().clear();
        event.setDroppedExp(0);
    }
}
