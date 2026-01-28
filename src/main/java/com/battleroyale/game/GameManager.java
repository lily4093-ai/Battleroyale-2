package com.battleroyale.game;

import com.battleroyale.BattleRoyalePlugin;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 게임의 핵심 로직을 관리하는 메인 매니저
 * - 게임 상태 관리
 * - 포인트 시스템
 * - 현상금 시스템
 * - 리스폰 시스템
 * - 데스타임 관리
 */
public class GameManager {

    private final BattleRoyalePlugin plugin;
    private GameState gameState;
    private final Map<UUID, PlayerData> playerDataMap;
    private final Map<UUID, List<UUID>> teams; // 팀 ID -> 플레이어 UUID 리스트
    private final Set<UUID> initialPlayers; // 게임 시작 시 참여한 플레이어

    // 현상금 시스템
    private UUID currentBountyTarget;
    private BossBar bountyBossBar;
    private BukkitTask bountyTask;
    private long bountyStartTime;
    private static final long BOUNTY_DURATION = 3 * 60 * 1000; // 3분
    private static final long BOUNTY_ACTIVATION_DELAY = 5 * 60 * 1000; // 5분 후 활성화

    // 게임 타이머
    private long gameStartTime;
    private BukkitTask gameTimerTask;
    private BukkitTask worldBorderTask;

    // 데스타임
    private boolean isDeathTime;
    private long deathTimeStartTime;
    private static final long DEATH_TIME_DURATION = 5 * 60 * 1000; // 5분

    // 리스폰 설정
    private static final long NORMAL_RESPAWN_TIME = 60 * 1000; // 1분
    private static final long DEATH_TIME_RESPAWN = 30 * 1000; // 30초

    // 스코어보드
    private ScoreboardManager scoreboardManager;
    private TabListManager tabListManager;
    private WorldBorderActionBarManager worldBorderActionBarManager;

    public GameManager(BattleRoyalePlugin plugin) {
        this.plugin = plugin;
        this.gameState = GameState.WAITING;
        this.playerDataMap = new HashMap<>();
        this.teams = new HashMap<>();
        this.initialPlayers = new HashSet<>();
        this.isDeathTime = false;
        this.scoreboardManager = new ScoreboardManager(plugin, this);
        this.tabListManager = new TabListManager(plugin, this);
        this.worldBorderActionBarManager = new WorldBorderActionBarManager(plugin);
    }

    /**
     * 게임 시작
     * 
     * @param teamSize 팀당 인원 수
     */
    public void startGame(int teamSize) {
        if (gameState != GameState.WAITING) {
            return;
        }

        gameState = GameState.STARTING;
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (players.isEmpty()) {
            Bukkit.broadcastMessage("§c[배틀로얄 2.0] 플레이어가 없어 게임을 시작할 수 없습니다!");
            gameState = GameState.WAITING;
            return;
        }

        // 초기 플레이어 기록
        initialPlayers.clear();
        players.forEach(p -> initialPlayers.add(p.getUniqueId()));

        // 팀 구성
        createTeams(players, teamSize);

        // 플레이어 데이터 초기화
        for (Player player : players) {
            PlayerData data = new PlayerData(player.getUniqueId());
            playerDataMap.put(player.getUniqueId(), data);
        }

        // 게임 시작
        gameState = GameState.ACTIVE;
        gameStartTime = System.currentTimeMillis();

        // 월드보더 먼저 설정 (스폰 위치 계산 전에 필요)
        setupWorldBorder();

        // 플레이어 스폰 (월드보더 설정 후)
        spawnPlayers();

        Bukkit.broadcastMessage("§a§l[배틀로얄 2.0] 게임이 시작되었습니다!");
        Bukkit.broadcastMessage("§e팀 크기: §f" + teamSize + "명");

        // 타이머 시작
        startGameTimers();

        // 보급품 투하 스케줄
        scheduleSupplyDrops();

        // 게임 규칙 표시
        displayGameRules();

        // 스코어보드 시작
        scoreboardManager.startUpdating();

        // TAB 리스트 시작
        tabListManager.startUpdating();

        // 월드보더 액션바 시작
        worldBorderActionBarManager.startUpdating();
    }

    /**
     * 팀 생성 및 배정
     */
    private void createTeams(List<Player> players, int teamSize) {
        teams.clear();
        Collections.shuffle(players);

        int teamCount = (int) Math.ceil((double) players.size() / teamSize);

        for (int i = 0; i < teamCount; i++) {
            UUID teamId = UUID.randomUUID();
            teams.put(teamId, new ArrayList<>());
        }

        List<UUID> teamIds = new ArrayList<>(teams.keySet());
        int currentTeamIndex = 0;

        for (Player player : players) {
            UUID teamId = teamIds.get(currentTeamIndex);
            teams.get(teamId).add(player.getUniqueId());

            PlayerData data = playerDataMap.get(player.getUniqueId());
            if (data != null) {
                data.setTeamId(teamId);
            }

            currentTeamIndex = (currentTeamIndex + 1) % teamIds.size();
        }
    }

    /**
     * 플레이어 스폰 (팀별로 같은 위치 또는 100블럭 이상 떨어진 위치)
     */
    private void spawnPlayers() {
        World world = Bukkit.getWorlds().get(0);
        Map<UUID, Location> teamSpawnLocations = new HashMap<>();

        for (Map.Entry<UUID, List<UUID>> entry : teams.entrySet()) {
            UUID teamId = entry.getKey();
            List<UUID> teamMembers = entry.getValue();

            // 팀 스폰 위치 결정
            Location spawnLoc = findSafeSpawnLocation(world, teamSpawnLocations.values());
            teamSpawnLocations.put(teamId, spawnLoc);

            // 팀원들을 같은 위치에 스폰
            for (UUID playerId : teamMembers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.teleport(spawnLoc);

                    // 최대 체력 설정 후 현재 체력 설정
                    double maxHealth = plugin.getConfigManager().getMaxHealth();
                    player.setMaxHealth(maxHealth);
                    player.setHealth(maxHealth);

                    player.setFoodLevel(20);
                    player.getInventory().clear();
                    player.setGameMode(GameMode.SURVIVAL);

                    // 보급품 탐지 나침반 및 보트 지급
                    giveSupplyCompass(player);
                    player.getInventory().addItem(new ItemStack(Material.OAK_BOAT));

                    // 스폰 시 3초 무적 (낙하 데미지 방지)
                    player.setInvulnerable(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.isOnline()) {
                                player.setInvulnerable(false);
                            }
                        }
                    }.runTaskLater(plugin, 60L); // 3초 (60틱)
                }
            }
        }
    }

    /**
     * 안전한 스폰 위치 찾기 (다른 팀과 최소 100블럭 떨어진 곳)
     */
    private Location findSafeSpawnLocation(World world, Collection<Location> existingLocations) {
        Random random = new Random();
        WorldBorder border = world.getWorldBorder();
        double size = border.getSize() / 2;

        for (int attempts = 0; attempts < 100; attempts++) {
            double x = random.nextDouble() * size * 2 - size;
            double z = random.nextDouble() * size * 2 - size;

            // 안전한 Y 좌표 찾기 (너무 높지 않은 곳)
            Location highestBlock = world.getHighestBlockAt((int) x, (int) z).getLocation();

            // 공중이나 나무 위가 아닌 실제 지면 찾기
            int y = highestBlock.getBlockY();
            while (y > 60 && (world.getBlockAt((int) x, y, (int) z).getType() == Material.AIR ||
                    world.getBlockAt((int) x, y, (int) z).getType().name().contains("LEAVES"))) {
                y--;
            }

            Location loc = new Location(world, x, y + 1, z);

            // 다른 팀과의 거리 확인
            boolean isSafe = true;
            for (Location existing : existingLocations) {
                if (loc.distance(existing) < 100) {
                    isSafe = false;
                    break;
                }
            }

            if (isSafe) {
                return loc;
            }
        }

        // 실패 시 랜덤 위치 반환
        double x = random.nextDouble() * size * 2 - size;
        double z = random.nextDouble() * size * 2 - size;
        return world.getHighestBlockAt((int) x, (int) z).getLocation().add(0, 1, 0);
    }

    /**
     * 게임 타이머 시작 (현상금, 데스타임 체크)
     */
    private void startGameTimers() {
        gameTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - gameStartTime;

                // 5분 후 현상금 시스템 활성화
                if (elapsed >= BOUNTY_ACTIVATION_DELAY && currentBountyTarget == null) {
                    startBountySystem();
                }

                // 데스타임 체크 (모든 초기 플레이어가 2번 이상 사망했는지)
                if (!isDeathTime && shouldStartDeathTime()) {
                    startDeathTime();
                }

                // 데스타임 종료 체크
                if (isDeathTime) {
                    long deathTimeElapsed = System.currentTimeMillis() - deathTimeStartTime;
                    if (deathTimeElapsed >= DEATH_TIME_DURATION) {
                        endGame();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // 1초마다 실행
    }

    /**
     * 월드보더 설정 및 축소
     * 단계별로 축소: 2500 → 2000 → 1500 → 1000 → 500 → 100 → 10 → 0
     * 축소 후 60초 대기, 초당 1.7블럭 속도로 축소
     */
    private void setupWorldBorder() {
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();

        // 중심을 (0, 0)으로 먼저 설정
        border.setCenter(0, 0);

        // 초기 크기 설정 (config에서 읽기)
        double initialSize = plugin.getConfigManager().getWorldBorderInitialSize();
        border.setSize(initialSize);

        Bukkit.broadcastMessage("§e[배틀로얄 2.0] 월드보더가 설정되었습니다: §f" + (int) initialSize + "x" + (int) initialSize);

        // config에서 수축 설정 읽기
        long shrinkInterval = plugin.getConfigManager().getWorldBorderShrinkInterval() * 20L; // 초를 틱으로 변환
        List<Integer> shrinkStages = plugin.getConfigManager().getWorldBorderShrinkStages();

        // 단계별 축소 스케줄
        worldBorderTask = new BukkitRunnable() {
            int currentStageIndex = 0;

            @Override
            public void run() {
                if (currentStageIndex >= shrinkStages.size() - 1) {
                    // 마지막 단계 (0)에 도달하면 게임 종료
                    cancel();
                    Bukkit.broadcastMessage("§c§l[배틀로얄 2.0] §e자기장이 완전히 축소되었습니다! 게임 종료!");
                    endGame();
                    return;
                }

                currentStageIndex++;
                double currentSize = border.getSize();
                double newSize = shrinkStages.get(currentStageIndex);

                // 축소할 거리 계산 (반지름 기준)
                double shrinkDistance = (currentSize - newSize) / 2;

                // 초당 1.7블럭 속도로 축소 시간 계산
                long shrinkDuration = (long) (shrinkDistance / 1.7);

                // 액션바 매니저에 축소 시작 알림
                worldBorderActionBarManager.notifyShrinkStart(currentSize, newSize, shrinkDuration);

                // 설정된 시간에 걸쳐 천천히 축소
                border.setSize(newSize, shrinkDuration);
            }
        }.runTaskTimer(plugin, shrinkInterval, shrinkInterval);
    }

    /**
     * 보급품 투하 스케줄
     * 게임 시작 직후 1회, 이후 5분마다 총 3회
     */
    private void scheduleSupplyDrops() {
        // 나침반 업데이트 시작
        plugin.getSupplyDropManager().startCompassUpdater();

        // 1차 투하 - 게임 시작 직후
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSupplyDropManager().startSupplyDrop(1);
            }
        }.runTaskLater(plugin, 20L); // 1초 후

        // 2차 투하 - 5분 후
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState == GameState.ACTIVE || gameState == GameState.DEATH_TIME) {
                    plugin.getSupplyDropManager().startSupplyDrop(2);
                }
            }
        }.runTaskLater(plugin, 5 * 60 * 20L);

        // 3차 투하 - 10분 후
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState == GameState.ACTIVE || gameState == GameState.DEATH_TIME) {
                    plugin.getSupplyDropManager().startSupplyDrop(3);
                }
            }
        }.runTaskLater(plugin, 10 * 60 * 20L);
    }

    /**
     * 게임 규칙 표시
     */
    private void displayGameRules() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§6§l========== [게임 규칙] ==========");
                Bukkit.broadcastMessage("§e1. §f시작 포인트: §a1000점");
                Bukkit.broadcastMessage("§e2. §f킬 보상: §a500 + 피해자 포인트 (현상금 2배)");
                Bukkit.broadcastMessage("§e3. §f사망 패널티: §c포인트 50% 감소");
                Bukkit.broadcastMessage("§e4. §f리스폰: §a1분 후 (데스타임 30초)");
                Bukkit.broadcastMessage("§e5. §f현상금: §c5분 후 활성화, 3분마다 교체");
                Bukkit.broadcastMessage("§e6. §f데스타임: §c모두 2번 사망 후 시작, 5분간 진행");
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§c§l[건축 규칙]");
                Bukkit.broadcastMessage("§7• §f블럭은 §e임시 엄폐용§f으로만 사용 가능");
                Bukkit.broadcastMessage("§7• §c땅을 파고 들어가는 행위 금지");
                Bukkit.broadcastMessage("§7• §c블럭을 쌓고 높이 올라가는 행위 금지");
                Bukkit.broadcastMessage("§7• §a달리면서 블럭 설치, 총알 막기용 설치 허용");
                Bukkit.broadcastMessage("§7• §a엄폐한 블럭 파괴는 허용");
                Bukkit.broadcastMessage("§6§l================================");
                Bukkit.broadcastMessage("");
            }
        }.runTaskLater(plugin, 40L); // 2초 후 표시
    }

    /**
     * 현상금 시스템 시작
     */
    private void startBountySystem() {
        selectNewBountyTarget();

        bountyTask = new BukkitRunnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - bountyStartTime;

                if (elapsed >= BOUNTY_DURATION) {
                    selectNewBountyTarget();
                }

                updateBountyBossBar();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * 새로운 현상금 대상 선정 (슬롯머신 연출)
     */
    private void selectNewBountyTarget() {
        // 이전 현상금 해제
        if (currentBountyTarget != null) {
            PlayerData oldData = playerDataMap.get(currentBountyTarget);
            if (oldData != null) {
                oldData.setBountyTarget(false);
            }
        }

        List<UUID> alivePlayers = playerDataMap.keySet().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .filter(uuid -> Bukkit.getPlayer(uuid).getGameMode() == GameMode.SURVIVAL)
                .collect(Collectors.toList());

        if (alivePlayers.isEmpty()) {
            return;
        }

        // 슬롯머신 연출
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60; // 3초간 연출

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // 최종 선정
                    UUID selected = alivePlayers.get(new Random().nextInt(alivePlayers.size()));
                    currentBountyTarget = selected;
                    bountyStartTime = System.currentTimeMillis();

                    PlayerData data = playerDataMap.get(selected);
                    if (data != null) {
                        data.setBountyTarget(true);
                    }

                    Player target = Bukkit.getPlayer(selected);
                    if (target != null) {
                        // 최종 타이틀 표시
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle("§c§l[ 현상금 ]", "§e" + target.getName(), 10, 40, 10);
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        }

                        Bukkit.broadcastMessage("§c§l[배틀로얄 2.0] §e" + target.getName() + "§f님이 현상금 수배자로 지정되었습니다!");
                    }

                    createBountyBossBar();
                    cancel();
                    return;
                }

                // 슬롯머신 효과
                UUID randomPlayer = alivePlayers.get(new Random().nextInt(alivePlayers.size()));
                Player p = Bukkit.getPlayer(randomPlayer);
                if (p != null) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.sendTitle("§c§l[ 현상금 ]", "§f" + p.getName(), 0, 5, 0);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * 현상금 보스바 생성
     */
    private void createBountyBossBar() {
        if (bountyBossBar != null) {
            bountyBossBar.removeAll();
        }

        Player target = Bukkit.getPlayer(currentBountyTarget);
        if (target == null)
            return;

        bountyBossBar = Bukkit.createBossBar(
                "§c§l현상금: §e" + target.getName(),
                BarColor.RED,
                BarStyle.SOLID);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bountyBossBar.addPlayer(p);
        }

        bountyBossBar.setVisible(true);
    }

    /**
     * 현상금 보스바 업데이트
     */
    private void updateBountyBossBar() {
        if (bountyBossBar == null || currentBountyTarget == null)
            return;

        Player target = Bukkit.getPlayer(currentBountyTarget);
        if (target == null) {
            selectNewBountyTarget();
            return;
        }

        long elapsed = System.currentTimeMillis() - bountyStartTime;
        double progress = 1.0 - ((double) elapsed / BOUNTY_DURATION);
        progress = Math.max(0.0, Math.min(1.0, progress));

        bountyBossBar.setProgress(progress);

        long remainingSeconds = (BOUNTY_DURATION - elapsed) / 1000;
        bountyBossBar.setTitle("§c§l현상금: §e" + target.getName() + " §f(" + remainingSeconds + "초 남음)");
    }

    /**
     * 데스타임 시작 조건 확인
     */
    private boolean shouldStartDeathTime() {
        for (UUID playerId : initialPlayers) {
            PlayerData data = playerDataMap.get(playerId);
            if (data == null || data.getDeathCount() < 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * 데스타임 시작
     */
    private void startDeathTime() {
        isDeathTime = true;
        deathTimeStartTime = System.currentTimeMillis();
        gameState = GameState.DEATH_TIME;

        Bukkit.broadcastMessage("§c§l=================================");
        Bukkit.broadcastMessage("§4§l         데스타임 시작!");
        Bukkit.broadcastMessage("§c모든 플레이어가 현상금 상태가 됩니다!");
        Bukkit.broadcastMessage("§c리스폰 시간이 30초로 단축됩니다!");
        Bukkit.broadcastMessage("§c5분 후 게임이 종료됩니다!");
        Bukkit.broadcastMessage("§c§l=================================");

        // 모든 플레이어를 현상금 상태로
        for (PlayerData data : playerDataMap.values()) {
            data.setBountyTarget(true);
        }

        // 현상금 보스바 숨김
        if (bountyBossBar != null) {
            bountyBossBar.setVisible(false);
        }
    }

    /**
     * 플레이어 사망 처리
     */
    public void handlePlayerDeath(Player victim, Player killer) {
        PlayerData victimData = playerDataMap.get(victim.getUniqueId());
        if (victimData == null)
            return;

        // 사망 횟수 증가
        victimData.incrementDeathCount();

        // 포인트 패널티 (50% 감소)
        int lostPoints = victimData.getPoints() / 2;
        victimData.removePoints(lostPoints);

        // 킬 보상 계산 및 분배
        if (killer != null) {
            distributeKillRewards(victim, killer, victimData);
        }

        // 데미지 기여도 초기화
        victimData.clearDamageContributions();

        // 리스폰 시간 설정
        long respawnDelay = isDeathTime ? DEATH_TIME_RESPAWN : NORMAL_RESPAWN_TIME;
        victimData.setRespawnTime(System.currentTimeMillis() + respawnDelay);

        // 현상금 대상이었다면 새로운 대상 선정
        if (victimData.isBountyTarget() && !isDeathTime) {
            victimData.setBountyTarget(false);
            selectNewBountyTarget();
        }

        // 리스폰 스케줄
        scheduleRespawn(victim, respawnDelay);
    }

    /**
     * 킬 보상 분배
     */
    private void distributeKillRewards(Player victim, Player killer, PlayerData victimData) {
        int baseReward = 500 + victimData.getPoints();

        // 현상금 대상이면 2배
        if (victimData.isBountyTarget()) {
            baseReward *= 2;
        }

        // 처치자에게 50% 즉시 지급
        PlayerData killerData = playerDataMap.get(killer.getUniqueId());
        if (killerData != null) {
            int killerReward = baseReward / 2;
            killerData.addPoints(killerReward);
            killer.sendMessage("§a[배틀로얄 2.0] 처치 보상: §e+" + killerReward + "점");
        }

        // 나머지 50%를 데미지 기여자에게 분배
        Map<UUID, Double> contributions = victimData.getDamageContributions();
        double totalDamage = victimData.getTotalDamage();

        if (totalDamage > 0) {
            int remainingReward = baseReward / 2;

            for (Map.Entry<UUID, Double> entry : contributions.entrySet()) {
                UUID contributorId = entry.getKey();
                double damage = entry.getValue();

                PlayerData contributorData = playerDataMap.get(contributorId);
                if (contributorData != null) {
                    int contributorReward = (int) ((damage / totalDamage) * remainingReward);
                    contributorData.addPoints(contributorReward);

                    Player contributor = Bukkit.getPlayer(contributorId);
                    if (contributor != null) {
                        contributor.sendMessage("§a[배틀로얄 2.0] 데미지 기여 보상: §e+" + contributorReward + "점");
                    }
                }
            }
        }

        // 킬 메시지
        String bountyText = victimData.isBountyTarget() ? " §c[현상금]" : "";
        Bukkit.broadcastMessage("§e[배틀로얄 2.0] §f" + killer.getName() + " §7→ §f" + victim.getName() + bountyText);
    }

    /**
     * 리스폰 스케줄
     */
    private void scheduleRespawn(Player player, long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    return;

                // 자기장 내 랜덤 위치에 리스폰
                World world = Bukkit.getWorlds().get(0);
                Location spawnLoc = findSafeSpawnLocation(world, Collections.emptyList());

                player.spigot().respawn();
                player.teleport(spawnLoc);

                // 최대 체력 설정 후 현재 체력 설정
                double maxHealth = plugin.getConfigManager().getMaxHealth();
                player.setMaxHealth(maxHealth);
                player.setHealth(maxHealth);

                player.setFoodLevel(20);
                player.setGameMode(GameMode.SURVIVAL);

                // 인벤토리 절반 삭제
                randomlyRemoveHalfInventory(player);

                // 리스폰 시 3초 무적
                player.setInvulnerable(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.setInvulnerable(false);
                        }
                    }
                }.runTaskLater(plugin, 60L); // 3초 (60틱)

                player.getInventory().addItem(new ItemStack(Material.OAK_BOAT));
                player.sendMessage("§a[배틀로얄 2.0] 부활했습니다! 보트가 지급되었습니다.");
            }
        }.runTaskLater(plugin, delay / 50); // 밀리초를 틱으로 변환
    }

    /**
     * 인벤토리 절반 랜덤 삭제
     */
    private void randomlyRemoveHalfInventory(Player player) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i) != null) {
                slots.add(i);
            }
        }

        Collections.shuffle(slots);
        int toRemove = slots.size() / 2;

        for (int i = 0; i < toRemove; i++) {
            player.getInventory().setItem(slots.get(i), null);
        }
    }

    /**
     * 게임 종료
     */
    public void endGame() {
        gameState = GameState.ENDING;

        // 최종 순위 계산
        List<Map.Entry<UUID, PlayerData>> rankings = playerDataMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getPoints(), a.getValue().getPoints()))
                .collect(Collectors.toList());

        Bukkit.broadcastMessage("§6§l=================================");
        Bukkit.broadcastMessage("§e§l       게임 종료!");
        Bukkit.broadcastMessage("§6§l=================================");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§e§l최종 순위:");

        for (int i = 0; i < Math.min(5, rankings.size()); i++) {
            Map.Entry<UUID, PlayerData> entry = rankings.get(i);
            Player p = Bukkit.getPlayer(entry.getKey());
            String name = p != null ? p.getName() : "Unknown";
            int points = entry.getValue().getPoints();

            String medal = i == 0 ? "§6🥇" : i == 1 ? "§7🥈" : i == 2 ? "§c🥉" : "§f" + (i + 1) + ".";
            Bukkit.broadcastMessage(medal + " §f" + name + " §e- " + points + "점");
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6§l=================================");

        // 정리
        cleanup();
    }

    /**
     * 게임 강제 종료
     */
    public void stopGame() {
        if (gameState == GameState.WAITING) {
            return;
        }

        Bukkit.broadcastMessage("§c[배틀로얄 2.0] 게임이 강제 종료되었습니다!");
        cleanup();
    }

    /**
     * 정리 작업
     */
    private void cleanup() {
        gameState = GameState.WAITING;

        if (gameTimerTask != null) {
            gameTimerTask.cancel();
        }

        if (bountyTask != null) {
            bountyTask.cancel();
        }

        if (worldBorderTask != null) {
            worldBorderTask.cancel();
        }

        if (bountyBossBar != null) {
            bountyBossBar.removeAll();
            bountyBossBar = null;
        }

        // 스코어보드 중지
        scoreboardManager.stopUpdating();

        // TAB 리스트 중지
        tabListManager.stopUpdating();

        // 월드보더 액션바 중지
        worldBorderActionBarManager.stopUpdating();

        // 보급품 시스템 정리
        plugin.getSupplyDropManager().cleanup();

        playerDataMap.clear();
        teams.clear();
        initialPlayers.clear();
        currentBountyTarget = null;
        isDeathTime = false;
    }

    /**
     * 플러그인 종료 시 호출
     */
    public void shutdown() {
        cleanup();
    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.get(playerId);
    }

    public boolean isDeathTime() {
        return isDeathTime;
    }

    public UUID getCurrentBountyTarget() {
        return currentBountyTarget;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public long getDeathTimeStartTime() {
        return deathTimeStartTime;
    }

    /**
     * 플레이어에게 보급품 탐지 나침반 지급
     */
    private void giveSupplyCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§l보급품 탐지기");
            meta.setLore(Arrays.asList(
                    "§7가장 가까운 미개봉 보급품을 가리킵니다",
                    "§7열린 보급품은 자동으로 제외됩니다"));
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
    }
}
