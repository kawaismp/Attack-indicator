package org.eneryleen.attackIndicator;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eneryleen.attackIndicator.indicator.legacy.TextFormatter;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LangManager {

    private final AttackIndicator plugin;
    private final Map<String, String> messages;
    private String language;

    public LangManager(AttackIndicator plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }

    public void loadLanguage(String lang) {
        // Sanitize the config-supplied code: only [a-zA-Z0-9_-] is allowed so a
        // value like "../../secret" can never resolve a file outside /lang.
        if (lang == null || !lang.matches("[a-zA-Z0-9_-]+")) {
            plugin.getLogger().warning("Invalid language code '" + lang + "', falling back to 'en'");
            lang = "en";
        }

        this.language = lang;
        messages.clear();

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, lang + ".yml");

        if (!langFile.exists()) {
            // saveResource throws if the resource isn't bundled, so only extract
            // a known language; otherwise (a non-bundled code that the operator
            // hasn't dropped a file for) fall back to English instead of crashing.
            if (plugin.getResource("lang/" + lang + ".yml") != null) {
                plugin.saveResource("lang/" + lang + ".yml", false);
            } else if (!"en".equals(lang)) {
                plugin.getLogger().warning("Language '" + lang + "' is not bundled and no file was provided; falling back to 'en'");
                loadLanguage("en");
                return;
            }
        }

        FileConfiguration config;
        try {
            config = YamlConfiguration.loadConfiguration(langFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load language file: " + lang + ".yml, using English");
            InputStream defaultStream = plugin.getResource("lang/en.yml");
            if (defaultStream != null) {
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            } else {
                return;
            }
        }

        loadMessages(config, "");
    }

    private void loadMessages(ConfigurationSection config, String path) {
        for (String key : config.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (config.isConfigurationSection(key)) {
                loadMessages(config.getConfigurationSection(key), fullPath);
            } else {
                messages.put(fullPath, config.getString(key, ""));
            }
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public String getFormatted(String key) {
        String message = getMessage(key);
        return TextFormatter.format(message);
    }

    public String getFormatted(String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        return TextFormatter.format(message);
    }

    public String getLanguage() {
        return language;
    }
}
