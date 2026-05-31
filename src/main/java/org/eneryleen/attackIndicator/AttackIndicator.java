package org.eneryleen.attackIndicator;

import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.eneryleen.attackIndicator.indicator.IndicatorFactory;
import org.eneryleen.attackIndicator.indicator.IndicatorSpawner;

public final class AttackIndicator extends JavaPlugin {

    private static AttackIndicator instance;
    private ConfigManager configManager;
    private LangManager langManager;
    private IndicatorSpawner indicatorManager;
    private PlayerToggleManager toggleManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        langManager = new LangManager(this);
        langManager.loadLanguage(configManager.getLanguage());

        toggleManager = new PlayerToggleManager(this);

        indicatorManager = IndicatorFactory.createIndicatorManager(this);

        getServer().getPluginManager().registerEvents(new DamageListener(this), this);

        ReloadCommand reloadCommand = new ReloadCommand(this);
        PluginCommand command = getCommand("attackindicator");
        if (command != null) {
            command.setExecutor(reloadCommand);
            command.setTabCompleter(reloadCommand);
        } else {
            getLogger().severe("Command 'attackindicator' is missing from plugin.yml; commands disabled.");
        }

        int pluginId = 27487;
        new Metrics(this, pluginId);

        UpdateChecker updateChecker = new UpdateChecker(this);
        updateChecker.checkForUpdates();

        getLogger().info("AttackIndicator v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (indicatorManager != null) {
            indicatorManager.cleanup();
        }

        getLogger().info("AttackIndicator disabled!");
    }

    public static AttackIndicator getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public IndicatorSpawner getIndicatorManager() {
        return indicatorManager;
    }

    public PlayerToggleManager getToggleManager() {
        return toggleManager;
    }
}
