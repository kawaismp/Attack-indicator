package org.eneryleen.attackIndicator;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final AttackIndicator plugin;
    private FileConfiguration config;

    private String language;
    private String indicatorFormat;
    private int displayDuration;
    private double upwardSpeed;
    private double verticalOffset;
    private boolean randomOffsetEnabled;
    private double randomOffsetX;
    private double randomOffsetY;
    private double randomOffsetZ;
    private DisplayMode displayMode;
    private Set<String> disabledWorlds;
    private boolean entityFilterWhitelist;
    private Set<EntityType> entityFilter;
    private boolean showOnPlayers;
    private float indicatorScale;
    private int maxActiveIndicators;

    public ConfigManager(AttackIndicator plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        language = config.getString("language", "en");
        indicatorFormat = config.getString("indicator-format", "<#ff5555>❤️ -{damage}");
        displayDuration = config.getInt("display-duration", 40);
        upwardSpeed = config.getDouble("upward-speed", 0.03);
        verticalOffset = config.getDouble("vertical-offset", -0.5);

        randomOffsetEnabled = config.getBoolean("random-offset.enabled", true);
        randomOffsetX = config.getDouble("random-offset.x", 0.5);
        randomOffsetY = config.getDouble("random-offset.y", 0.5);
        randomOffsetZ = config.getDouble("random-offset.z", 0.5);

        String mode = config.getString("display-mode", "PLAYER_ONLY");
        try {
            displayMode = DisplayMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid display-mode: " + mode + ", using PLAYER_ONLY");
            displayMode = DisplayMode.PLAYER_ONLY;
        }

        disabledWorlds = new HashSet<>(config.getStringList("disabled-worlds"));

        showOnPlayers = config.getBoolean("show-on-players", false);
        indicatorScale = (float) config.getDouble("indicator-scale", 1.5);

        // Hard cap on concurrently spawned indicators to bound entity/tick load
        // during damage bursts. Values <= 0 disable the cap (unlimited).
        maxActiveIndicators = config.getInt("performance.max-active-indicators", 200);

        entityFilterWhitelist = config.getBoolean("entity-filter.whitelist-mode", false);
        entityFilter = new HashSet<>();
        List<String> entityList = config.getStringList("entity-filter.entities");
        for (String entityName : entityList) {
            try {
                EntityType type = EntityType.valueOf(entityName.toUpperCase());
                entityFilter.add(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type: " + entityName);
            }
        }
    }

    public String getLanguage() {
        return language;
    }

    public String getIndicatorFormat() {
        return indicatorFormat;
    }

    public int getDisplayDuration() {
        return displayDuration;
    }

    public double getUpwardSpeed() {
        return upwardSpeed;
    }

    public double getVerticalOffset() {
        return verticalOffset;
    }

    public boolean isRandomOffsetEnabled() {
        return randomOffsetEnabled;
    }

    public double getRandomOffsetX() {
        return randomOffsetX;
    }

    public double getRandomOffsetY() {
        return randomOffsetY;
    }

    public double getRandomOffsetZ() {
        return randomOffsetZ;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public Set<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public boolean isEntityFilterWhitelist() {
        return entityFilterWhitelist;
    }

    public Set<EntityType> getEntityFilter() {
        return entityFilter;
    }

    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds.contains(worldName);
    }

    public boolean shouldShowForEntity(EntityType type) {
        if (entityFilter.isEmpty()) {
            return true;
        }

        if (entityFilterWhitelist) {
            return entityFilter.contains(type);
        } else {
            return !entityFilter.contains(type);
        }
    }

    public boolean isShowOnPlayers() {
        return showOnPlayers;
    }

    public float getIndicatorScale() {
        return indicatorScale;
    }

    public int getMaxActiveIndicators() {
        return maxActiveIndicators;
    }

    public enum DisplayMode {
        ALL,
        PLAYER_ONLY,
        NO_SELF
    }
}
