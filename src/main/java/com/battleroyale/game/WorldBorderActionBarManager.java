package com.battleroyale.game;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * 자기장(월드보더) 정보를 액션바에 표시하는 매니저
 */
public class WorldBorderActionBarManager {

    private final BattleRoyalePlugin plugin;
    private BukkitTask updateTask;

    private double previousSize = 0;
    private double targetSize = 0;
    private long shrinkStartTime = 0;
    private long shrinkDuration = 0;
    private boolean isShrinking = false;

    public WorldBorderActionBarManager(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 액션바 업데이트 시작
     */
    public void startUpdating() {
        if (updateTask != null && !updateTask.isCancelled()) {
            return;
        }

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateActionBar();
            }
        }.runTaskTimer(plugin, 0L, 5L); // 5틱마다 업데이트 (0.25초)
    }

    /**
     * 액션바 업데이트 중지
     */
    public void stopUpdating() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    /**
     * 축소 시작 알림
     */
    public void notifyShrinkStart(double fromSize, double toSize, long durationSeconds) {
        this.previousSize = fromSize;
        this.targetSize = toSize;
        this.shrinkStartTime = System.currentTimeMillis();
        this.shrinkDuration = durationSeconds * 1000;
        this.isShrinking = true;
    }

    /**
     * 액션바 업데이트
     */
    private void updateActionBar() {
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        double currentSize = border.getSize();
        double centerX = border.getCenter().getX();
        double centerZ = border.getCenter().getZ();

        // 축소 진행률 계산
        double progress = 0.0;
        if (isShrinking && shrinkDuration > 0) {
            long elapsed = System.currentTimeMillis() - shrinkStartTime;
            progress = Math.min(100.0, (elapsed / (double) shrinkDuration) * 100.0);

            // 축소 완료 확인
            if (elapsed >= shrinkDuration) {
                isShrinking = false;
                previousSize = targetSize;
            }
        }

        String message;
        if (isShrinking) {
            // 축소 중일 때
            message = String.format(
                    "§7자기장 크기: §c%.0f §7→ §c%.0f §f| §7축소 진행률: §c%.0f%% §f| §7자기장 중심: §c(%.0f, %.0f) §f| §7현재 크기: §e%.0f",
                    previousSize, targetSize, progress, centerX, centerZ, currentSize);
        } else {
            // 대기 중일 때
            message = String.format(
                    "§7자기장 중심: §c(%.0f, %.0f) §f| §7현재 크기: §e%.0f",
                    centerX, centerZ, currentSize);
        }

        // 모든 플레이어에게 액션바 전송
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.SPECTATOR) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        }
    }
}
