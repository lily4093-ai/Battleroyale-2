package com.battleroyale;

import com.battleroyale.commands.BRCommand;
import com.battleroyale.config.ConfigManager;
import com.battleroyale.game.GameManager;
import com.battleroyale.listeners.*;
import com.battleroyale.supply.SupplyDropManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleRoyalePlugin extends JavaPlugin {

    private static BattleRoyalePlugin instance;
    private ConfigManager configManager;
    private GameManager gameManager;
    private SupplyDropManager supplyDropManager;

    @Override
    public void onEnable() {
        instance = this;

        // 설정 파일 로드
        configManager = new ConfigManager(this);

        // Initialize managers
        gameManager = new GameManager(this);
        supplyDropManager = new SupplyDropManager(this);

        // Register commands
        BRCommand brCommand = new BRCommand(this);
        getCommand("br").setExecutor(brCommand);
        getCommand("br").setTabCompleter(brCommand);

        // Register listeners
        registerListeners();

        getLogger().info("§a[배틀로얄 2.0] 플러그인이 활성화되었습니다!");
        getLogger().info("§e기본 월드를 사용합니다. (구조물 비활성화는 server.properties 등을 확인해 주세요)");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdown();
        }
        getLogger().info("§c[배틀로얄 2.0] 플러그인이 비활성화되었습니다!");
    }

    /*
     * @Override
     * public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
     * return new BRWorldGenerator();
     * }
     */

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new SupplyDropListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockRestrictionListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkLoadListener(this), this);
    }

    public static BattleRoyalePlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public SupplyDropManager getSupplyDropManager() {
        return supplyDropManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
