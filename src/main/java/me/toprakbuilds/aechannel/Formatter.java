package me.toprakbuilds.aechannel;

import org.bukkit.ChatColor;

public class Formatter {
    public static String color(String message) {
        return message == null ? "" : ChatColor.translateAlternateColorCodes('&', message);
    }
}