package com.battleroyale.commands;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.game.GameManager;
import com.battleroyale.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /br 명령어 처리
 * - /br start <팀원 수> : 게임 시작
 * - /br stop : 게임 강제 종료
 * - /br info : 게임 정보 확인
 */
public class BRCommand implements CommandExecutor, TabCompleter {

    private final BattleRoyalePlugin plugin;

    public BRCommand(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("battleroyale.admin")) {
            sender.sendMessage("§c[배틀로얄 2.0] 권한이 없습니다!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        GameManager gameManager = plugin.getGameManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 2) {
                    sender.sendMessage("§c[배틀로얄 2.0] 사용법: /br start <팀원 수>");
                    return true;
                }

                try {
                    int teamSize = Integer.parseInt(args[1]);

                    if (teamSize < 1) {
                        sender.sendMessage("§c[배틀로얄 2.0] 팀원 수는 1명 이상이어야 합니다!");
                        return true;
                    }

                    if (gameManager.getGameState() != GameState.WAITING) {
                        sender.sendMessage("§c[배틀로얄 2.0] 이미 게임이 진행 중입니다!");
                        return true;
                    }

                    gameManager.startGame(teamSize);
                    sender.sendMessage("§a[배틀로얄 2.0] 게임을 시작합니다! (팀원 수: " + teamSize + "명)");

                } catch (NumberFormatException e) {
                    sender.sendMessage("§c[배틀로얄 2.0] 올바른 숫자를 입력해주세요!");
                }
                break;

            case "stop":
                if (gameManager.getGameState() == GameState.WAITING) {
                    sender.sendMessage("§c[배틀로얄 2.0] 진행 중인 게임이 없습니다!");
                    return true;
                }

                gameManager.stopGame();
                sender.sendMessage("§a[배틀로얄 2.0] 게임을 강제 종료했습니다!");
                break;

            case "info":
                sendGameInfo(sender, gameManager);
                break;

            case "reload":
                plugin.getConfigManager().reloadConfig();
                sender.sendMessage("§a[배틀로얄 2.0] 설정 파일을 리로드했습니다!");
                sender.sendMessage("§7주의: 게임 진행 중에는 일부 설정이 적용되지 않을 수 있습니다.");
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    /**
     * 도움말 메시지
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l========== [배틀로얄 2.0] ==========");
        sender.sendMessage("§e/br start <팀원 수> §7- 게임 시작");
        sender.sendMessage("§e/br stop §7- 게임 강제 종료");
        sender.sendMessage("§e/br info §7- 게임 정보 확인");
        sender.sendMessage("§e/br reload §7- 설정 파일 리로드");
        sender.sendMessage("§6§l================================");
    }

    /**
     * 게임 정보 표시
     */
    private void sendGameInfo(CommandSender sender, GameManager gameManager) {
        sender.sendMessage("§6§l========== [게임 정보] ==========");
        sender.sendMessage("§e게임 상태: §f" + getStateString(gameManager.getGameState()));
        sender.sendMessage("§e데스타임: §f" + (gameManager.isDeathTime() ? "§c활성화" : "§a비활성화"));

        if (gameManager.getCurrentBountyTarget() != null) {
            Player bounty = plugin.getServer().getPlayer(gameManager.getCurrentBountyTarget());
            String bountyName = bounty != null ? bounty.getName() : "알 수 없음";
            sender.sendMessage("§e현상금 대상: §c" + bountyName);
        } else {
            sender.sendMessage("§e현상금 대상: §7없음");
        }

        sender.sendMessage("§6§l================================");
    }

    /**
     * 게임 상태를 문자열로 변환
     */
    private String getStateString(GameState state) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("start", "stop", "info", "reload"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
        }

        return completions;
    }
}
