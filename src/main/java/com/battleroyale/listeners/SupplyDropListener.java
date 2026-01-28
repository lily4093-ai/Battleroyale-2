package com.battleroyale.listeners;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

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

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof org.bukkit.block.Chest) {
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) holder;
            Block block = chest.getBlock();

            // 보급품 상자인지 확인
            if (plugin.getSupplyDropManager().isSupplyCrate(block.getLocation())) {
                // 열림 상태로 표시 (나침반에서 제외)
                plugin.getSupplyDropManager().markSupplyOpened(block.getLocation());

                // 상자는 파괴하지 않고 그대로 둠 (열린 상태로만 표시)
            }
        }
    }
}
