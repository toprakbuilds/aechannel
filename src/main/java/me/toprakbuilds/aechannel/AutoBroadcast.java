package me.toprakbuilds.aechannel;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;

public class AutoBroadcast extends BukkitRunnable {
    private final AEChannel plugin;
    private int index = 0;

    public AutoBroadcast(AEChannel plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        List<String> messages = plugin.getConfig().getStringList("announcements.messages");
        if (messages.isEmpty()) return;

        String raw = messages.get(index);
        String prefix = plugin.getConfig().getString("announcements.prefix");

        // Mesajı \n karakterine göre parçalara ayırıyoruz
        String[] lines = raw.split("\\n");

        for (String line : lines) {
            Bukkit.broadcastMessage(plugin.color(prefix + " " + line));
        }

        // ... (Action Bar kısmı tek satır kalmalı, oraya sadece ilk satırı gönderebiliriz)
        index = (index + 1) % messages.size();
    }
}