package com.battleroyale.game;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * TAB 리스트 관리 클래스
 * 플레이어 이름 옆에 포인트 및 현상금 표시
 */
public class TabListManager {

    private final BattleRoyalePlugin plugin;
    private final GameManager gameManager;
    private BukkitTask updateTask;

    public TabListManager(BattleRoyalePlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    /**
     * TAB 리스트 업데이트 시작
     */
    public void startUpdating() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateTabList();
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1초마다 업데이트
    }

    /**
     * TAB 리스트 업데이트 중지
     */
    public void stopUpdating() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        // 모든 플레이어의 TAB 리스트 초기화
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playerListName(LegacyComponentSerializer.legacySection().deserialize(player.getName()));
        }
    }

    /**
     * TAB 리스트 업데이트
     */
    private void updateTabList() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = gameManager.getPlayerData(player.getUniqueId());

            if (data == null) {
                // 게임에 참여하지 않은 플레이어 (관전자)
                player.playerListName(LegacyComponentSerializer.legacySection().deserialize("§7" + player.getName()));
                continue;
            }

            // 포인트 표시
            String pointsText = "§e" + data.getPoints() + "점";

            // 현상금 표시
            String bountyText = data.isBountyTarget() ? " §c[현상금]" : "";

            // TAB 리스트 이름 설정
            String displayName = "§f" + player.getName() + " §7| " + pointsText + bountyText;

            player.playerListName(LegacyComponentSerializer.legacySection().deserialize(displayName));
        }
    }
}
