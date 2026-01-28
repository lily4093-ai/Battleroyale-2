package com.battleroyale.game;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

/**
 * 스코어보드 관리 클래스
 * 오른쪽에 게임 스탯 표시
 */
public class ScoreboardManager {

    private final BattleRoyalePlugin plugin;
    private final GameManager gameManager;
    private BukkitTask updateTask;

    public ScoreboardManager(BattleRoyalePlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    /**
     * 스코어보드 업데이트 시작
     */
    public void startUpdating() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1초마다 업데이트
    }

    /**
     * 스코어보드 업데이트 중지
     */
    public void stopUpdating() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        // 모든 플레이어의 스코어보드 제거
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    /**
     * 플레이어 스코어보드 업데이트
     */
    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("battleroyale", "dummy", "§6§l배틀로얄 2.0");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        GameState state = gameManager.getGameState();
        PlayerData playerData = gameManager.getPlayerData(player.getUniqueId());

        int line = 15;

        // 빈 줄
        objective.getScore("§" + line--).setScore(line);

        // 게임 상태
        String stateText = getStateText(state);
        objective.getScore("§e게임: §f" + stateText).setScore(line--);

        // 경과 시간
        if (state == GameState.ACTIVE || state == GameState.DEATH_TIME) {
            long elapsed = (System.currentTimeMillis() - gameManager.getGameStartTime()) / 1000;
            long minutes = elapsed / 60;
            long seconds = elapsed % 60;
            objective.getScore("§e시간: §f" + minutes + "분 " + seconds + "초").setScore(line--);
        }

        // 빈 줄
        objective.getScore("§" + line--).setScore(line);

        // 플레이어 정보
        if (playerData != null) {
            objective.getScore("§a내 포인트: §f" + playerData.getPoints()).setScore(line--);
            objective.getScore("§c사망 횟수: §f" + playerData.getDeathCount()).setScore(line--);

            // 현상금 여부
            if (playerData.isBountyTarget()) {
                objective.getScore("§c§l현상금 대상!").setScore(line--);
            }
        }

        // 빈 줄
        objective.getScore("§" + line--).setScore(line);

        // 데스타임 정보
        if (gameManager.isDeathTime()) {
            long deathTimeElapsed = (System.currentTimeMillis() - gameManager.getDeathTimeStartTime()) / 1000;
            long remaining = (5 * 60) - deathTimeElapsed;
            long mins = remaining / 60;
            long secs = remaining % 60;
            objective.getScore("§4§l데스타임").setScore(line--);
            objective.getScore("§c남은 시간: §f" + mins + ":" + String.format("%02d", secs)).setScore(line--);
        }

        // 현상금 정보
        if (!gameManager.isDeathTime() && gameManager.getCurrentBountyTarget() != null) {
            Player bountyTarget = Bukkit.getPlayer(gameManager.getCurrentBountyTarget());
            if (bountyTarget != null) {
                objective.getScore("§c현상금: §e" + bountyTarget.getName()).setScore(line--);
            }
        }

        // 빈 줄
        objective.getScore("§" + line--).setScore(line);

        // 생존 플레이어 수
        long alivePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p -> gameManager.getPlayerData(p.getUniqueId()) != null)
                .filter(p -> p.getGameMode() == org.bukkit.GameMode.SURVIVAL)
                .count();
        objective.getScore("§a생존: §f" + alivePlayers + "명").setScore(line--);

        // 빈 줄
        objective.getScore("§" + line).setScore(line);

        player.setScoreboard(scoreboard);
    }

    /**
     * 게임 상태를 텍스트로 변환
     */
    private String getStateText(GameState state) {
        switch (state) {
            case WAITING:
                return "§7대기 중";
            case STARTING:
                return "§e시작 중";
            case ACTIVE:
                return "§a진행 중";
            case DEATH_TIME:
                return "§c데스타임";
            case ENDING:
                return "§6종료 중";
            default:
                return "§7알 수 없음";
        }
    }
}
