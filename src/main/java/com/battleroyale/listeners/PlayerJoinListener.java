package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.game.GameManager;
import com.battleroyale.game.GameState;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 플레이어 접속 이벤트 처리
 */
public class PlayerJoinListener implements Listener {
    
    private final BattleRoyalePlugin plugin;
    
    public PlayerJoinListener(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = plugin.getGameManager();
        
        // 최대 체력 60 HP (30하트) 설정
        double maxHealth = plugin.getConfigManager().getMaxHealth();
        player.setMaxHealth(maxHealth);
        player.setHealth(maxHealth);
        
        // 게임이 진행 중이면 관전자 모드로 전환
        if (gameManager.getGameState() == GameState.ACTIVE || 
            gameManager.getGameState() == GameState.DEATH_TIME) {
            
            // 초기 플레이어가 아니면 관전자로
            if (gameManager.getPlayerData(player.getUniqueId()) == null) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("§e[배틀로얄 2.0] 게임이 진행 중입니다. 관전 모드로 전환됩니다.");
            }
        }
    }
}
