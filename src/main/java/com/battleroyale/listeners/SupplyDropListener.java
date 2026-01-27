package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * 보급품 상자 관련 이벤트 처리
 */
public class SupplyDropListener implements Listener {
    
    private final BattleRoyalePlugin plugin;
    
    public SupplyDropListener(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // 보급품 상자는 파괴 가능하도록 허용
        // 추가 로직이 필요하면 여기에 구현
    }
}
