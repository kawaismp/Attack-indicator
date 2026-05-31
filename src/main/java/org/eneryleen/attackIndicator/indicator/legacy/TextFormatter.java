package org.eneryleen.attackIndicator.indicator.legacy;

import org.bukkit.ChatColor;

public class TextFormatter {

    private static Boolean adventureAvailable = null;
    private static Object miniMessage = null;
    private static Object legacySerializer = null;

    public static String format(String text) {
        if (isAdventureAvailable()) {
            return formatWithAdventure(text);
        } else {
            return formatWithChatColor(text);
        }
    }

    private static boolean isAdventureAvailable() {
        if (adventureAvailable == null) {
            try {
                Class<?> miniMessageClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
                Class<?> legacySerializerClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");

                miniMessage = miniMessageClass.getMethod("miniMessage").invoke(null);
                legacySerializer = legacySerializerClass.getMethod("legacySection").invoke(null);

                adventureAvailable = true;
            } catch (Exception e) {
                adventureAvailable = false;
            }
        }
        return adventureAvailable;
    }

    private static String formatWithAdventure(String text) {
        try {
            Object component = miniMessage.getClass()
                    .getMethod("deserialize", String.class)
                    .invoke(miniMessage, text);

            return (String) legacySerializer.getClass()
                    .getMethod("serialize", Class.forName("net.kyori.adventure.text.Component"))
                    .invoke(legacySerializer, component);
        } catch (Exception e) {
            return formatWithChatColor(text);
        }
    }

    private static String formatWithChatColor(String text) {
        text = text.replaceAll("<#[0-9a-fA-F]{6}>", "");
        text = text.replaceAll("</[^>]+>", "");
        text = text.replaceAll("<gradient:[^>]+>", "");
        text = text.replaceAll("</gradient>", "");

        text = text.replace("<red>", ChatColor.RED.toString());
        text = text.replace("<dark_red>", ChatColor.DARK_RED.toString());
        text = text.replace("<gold>", ChatColor.GOLD.toString());
        text = text.replace("<yellow>", ChatColor.YELLOW.toString());
        text = text.replace("<green>", ChatColor.GREEN.toString());
        text = text.replace("<dark_green>", ChatColor.DARK_GREEN.toString());
        text = text.replace("<aqua>", ChatColor.AQUA.toString());
        text = text.replace("<dark_aqua>", ChatColor.DARK_AQUA.toString());
        text = text.replace("<blue>", ChatColor.BLUE.toString());
        text = text.replace("<dark_blue>", ChatColor.DARK_BLUE.toString());
        text = text.replace("<light_purple>", ChatColor.LIGHT_PURPLE.toString());
        text = text.replace("<dark_purple>", ChatColor.DARK_PURPLE.toString());
        text = text.replace("<white>", ChatColor.WHITE.toString());
        text = text.replace("<gray>", ChatColor.GRAY.toString());
        text = text.replace("<dark_gray>", ChatColor.DARK_GRAY.toString());
        text = text.replace("<black>", ChatColor.BLACK.toString());

        text = text.replace("<bold>", ChatColor.BOLD.toString());
        text = text.replace("<italic>", ChatColor.ITALIC.toString());
        text = text.replace("<underlined>", ChatColor.UNDERLINE.toString());
        text = text.replace("<strikethrough>", ChatColor.STRIKETHROUGH.toString());
        text = text.replace("<obfuscated>", ChatColor.MAGIC.toString());
        text = text.replace("<reset>", ChatColor.RESET.toString());

        text = ChatColor.translateAlternateColorCodes('&', text);

        return text;
    }
}
