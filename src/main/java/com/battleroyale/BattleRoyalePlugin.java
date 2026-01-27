package com.battleroyale;

import com.battleroyale.commands.BRCommand;
import com.battleroyale.config.ConfigManager;
import com.battleroyale.game.GameManager;
import com.battleroyale.listeners.*;
import com.battleroyale.supply.SupplyDropManager;
import com.battleroyale.world.BRWorldGenerator;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
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
        getCommand("br").setExecutor(new BRCommand(this));

        // Register listeners
        registerListeners();

        getLogger().info("§a[배틀로얄 2.0] 플러그인이 활성화되었습니다!");
        getLogger().info("§e커스텀 월드 생성기가 등록되었습니다. (바다, 동굴, 구조물 없음)");
    }
    
    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdown();
        }
        getLogger().info("§c[배틀로얄 2.0] 플러그인이 비활성화되었습니다!");
    }
    
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new BRWorldGenerator();
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new SupplyDropListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockRestrictionListener(this), this);
        getServer().getPluginManager().registerEvents(new MobSpawnListener(this), this);
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
