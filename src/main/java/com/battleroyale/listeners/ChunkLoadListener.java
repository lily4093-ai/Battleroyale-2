package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * 청크 로드 시 대기 중인 보급품을 실제로 생성하는 리스너
 */
public class ChunkLoadListener implements Listener {

    private final BattleRoyalePlugin plugin;

    public ChunkLoadListener(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // 청크 로드 시 해당 청크에 생성되어야 할 보급품이 있다면 생성
        plugin.getSupplyDropManager().realizePendingDropsInChunk(event.getChunk());
    }
}
