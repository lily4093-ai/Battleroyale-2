package com.battleroyale.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * 몹 스폰 제어 리스너
 * - 자연 스폰, 스포너 등 모든 몹 스폰 방지
 */
public class MobSpawnListener implements Listener {

    public MobSpawnListener() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        SpawnReason reason = event.getSpawnReason();

        // 플러그인/명령어로 소환된 것은 허용
        if (reason == SpawnReason.CUSTOM || reason == SpawnReason.COMMAND) {
            return;
        }

        // 플레이어는 허용
        if (event.getEntityType() == EntityType.PLAYER) {
            return;
        }

        // 그 외 모든 몹 스폰 차단
        event.setCancelled(true);
    }
}
