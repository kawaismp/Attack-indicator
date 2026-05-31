package org.eneryleen.attackIndicator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class UpdateChecker {

    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/%s/version";
    private static final String PROJECT_ID = "attack-indicator";

    private final JavaPlugin plugin;
    private final Logger logger;
    private final String currentVersion;
    private final String userAgent;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.currentVersion = plugin.getDescription().getVersion();
        this.userAgent = "AttackIndicator/" + currentVersion + " (github.com/Eneryleen/Attack-indicator)";
    }

    public void checkForUpdates() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                String apiUrl = String.format(MODRINTH_API_URL, PROJECT_ID);
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", userAgent);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    logger.warning("Failed to check for updates: HTTP " + responseCode);
                    return;
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                // Bound the buffer so a compromised/hijacked endpoint streaming an
                // unbounded body cannot OOM the server.
                final int maxResponseChars = 1_048_576; // 1 MB
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    if (response.length() > maxResponseChars) {
                        logger.warning("Update check aborted: response exceeded " + maxResponseChars + " characters");
                        return;
                    }
                }

                JsonArray versions = JsonParser.parseString(response.toString()).getAsJsonArray();
                if (versions.size() == 0) {
                    return;
                }

                JsonElement latestVersionElement = versions.get(0).getAsJsonObject().get("version_number");
                String latestVersion = latestVersionElement.getAsString();

                if (!currentVersion.equals(latestVersion)) {
                    logger.warning("╔══════════════════════════════════════════════════════╗");
                    logger.warning("║                                                      ║");
                    logger.warning("║   A new version of AttackIndicator is available!     ║");
                    logger.warning("║                                                      ║");
                    logger.warning("║   Current version: " + String.format("%-27s", currentVersion) + "       ║");
                    logger.warning("║   Latest version:  " + String.format("%-27s", latestVersion) + "       ║");
                    logger.warning("║                                                      ║");
                    logger.warning("║   https://modrinth.com/plugin/attack-indicator       ║");
                    logger.warning("║                                                      ║");
                    logger.warning("╚══════════════════════════════════════════════════════╝");
                } else {
                    logger.info("AttackIndicator is up to date!");
                }

            } catch (Exception e) {
                logger.warning("Failed to check for updates: " + e.getMessage());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ignored) {
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
