package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.game.GameManager;
import com.battleroyale.game.GameState;
import com.battleroyale.game.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * 플레이어 데미지 추적 (킬 기여도 계산용)
 */
public class PlayerDamageListener implements Listener {
    
    private final BattleRoyalePlugin plugin;
    
    public PlayerDamageListener(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        
        GameManager gameManager = plugin.getGameManager();
        
        // 게임이 진행 중일 때만 추적
        if (gameManager.getGameState() != GameState.ACTIVE && 
            gameManager.getGameState() != GameState.DEATH_TIME) {
            return;
        }
        
        // 같은 팀이면 데미지 무효화 (팀킬 방지)
        PlayerData victimData = gameManager.getPlayerData(victim.getUniqueId());
        PlayerData attackerData = gameManager.getPlayerData(attacker.getUniqueId());
        
        if (victimData != null && attackerData != null) {
            if (victimData.getTeamId() != null && 
                victimData.getTeamId().equals(attackerData.getTeamId())) {
                event.setCancelled(true);
                attacker.sendMessage("§c[배틀로얄 2.0] 팀원을 공격할 수 없습니다!");
                return;
            }
        }
        
        // 데미지 기여도 기록
        if (victimData != null) {
            double damage = event.getFinalDamage();
            victimData.addDamage(attacker.getUniqueId(), damage);
        }
        
        // 현상금 대상의 공격력 1.5배
        if (attackerData != null && attackerData.isBountyTarget()) {
            event.setDamage(event.getDamage() * 1.5);
        }
        
        // 데스타임에는 모두 1.5배
        if (gameManager.isDeathTime()) {
            event.setDamage(event.getDamage() * 1.5);
        }
    }
}
