package com.battleroyale.game;

import org.bukkit.entity.Player;

import java.util.*;

/**
 * 플레이어 데이터 관리 (포인트, 데미지 추적, 사망 횟수)
 */
public class PlayerData {
    
    private final UUID playerId;
    private int points;
    private int deathCount;
    private boolean isBountyTarget;
    private long respawnTime;
    private UUID teamId;
    
    // 킬 기여도를 위한 데미지 추적
    private final Map<UUID, Double> damageContributions;
    
    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.points = 1000; // 시작 포인트
        this.deathCount = 0;
        this.isBountyTarget = false;
        this.respawnTime = 0;
        this.damageContributions = new HashMap<>();
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public int getPoints() {
        return points;
    }
    
    public void setPoints(int points) {
        this.points = Math.max(0, points);
    }
    
    public void addPoints(int amount) {
        this.points += amount;
    }
    
    public void removePoints(int amount) {
        this.points = Math.max(0, this.points - amount);
    }
    
    public int getDeathCount() {
        return deathCount;
    }
    
    public void incrementDeathCount() {
        this.deathCount++;
    }
    
    public boolean isBountyTarget() {
        return isBountyTarget;
    }
    
    public void setBountyTarget(boolean bountyTarget) {
        this.isBountyTarget = bountyTarget;
    }
    
    public long getRespawnTime() {
        return respawnTime;
    }
    
    public void setRespawnTime(long respawnTime) {
        this.respawnTime = respawnTime;
    }
    
    public UUID getTeamId() {
        return teamId;
    }
    
    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }
    
    // 데미지 추적 메서드
    public void addDamage(UUID attackerId, double damage) {
        damageContributions.merge(attackerId, damage, Double::sum);
    }
    
    public Map<UUID, Double> getDamageContributions() {
        return new HashMap<>(damageContributions);
    }
    
    public void clearDamageContributions() {
        damageContributions.clear();
    }
    
    public double getTotalDamage() {
        return damageContributions.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
