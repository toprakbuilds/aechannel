package me.toprakbuilds.aechannel;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class ModCommands implements CommandExecutor, TabCompleter {
    private final AEChannel plugin;
    public ModCommands(AEChannel plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("aechannel.moderator")) return true;

        if (cmd.getName().equalsIgnoreCase("sustur")) {
            if (args.length < 3) { sender.sendMessage(plugin.color("&cKullanım: /sustur <oyuncu> <saniye> <neden>")); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;

            long duration = Long.parseLong(args[1]);
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            PlayerSettings s = plugin.getSettings(target);
            s.muteEnd = System.currentTimeMillis() + (duration * 1000L);
            s.muteReason = reason;
            plugin.getDb().save(target.getUniqueId(), s);

            sender.sendMessage(plugin.color("&a" + target.getName() + " susturuldu."));

            if (plugin.getConfig().getBoolean("discord-log-enabled")) {
                String webhook = plugin.getDiscordConfig().getString("channels.mute");
                String format = plugin.getDiscordConfig().getString("messages.mute-format")
                        .replace("%staff%", sender.getName())
                        .replace("%target%", target.getName())
                        .replace("%reason%", reason);
                DiscordWebhook.send(webhook, format);
            }
        }
        else if (cmd.getName().equalsIgnoreCase("sustur-ac")) {
            if (args.length < 1) return true;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;

            PlayerSettings s = plugin.getSettings(target);
            s.muteEnd = 0;
            s.warnCount = 0;
            plugin.getDb().save(target.getUniqueId(), s);
            sender.sendMessage(plugin.color("&aCezası kaldırıldı."));

            if (plugin.getConfig().getBoolean("discord-log-enabled")) {
                DiscordWebhook.send(plugin.getDiscordConfig().getString("channels.mute"), "🔓 **[UNMUTE]** " + sender.getName() + " -> " + target.getName());
            }
        }
        else if (cmd.getName().equalsIgnoreCase("chatsustur")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("ac")) {
                plugin.setChatLocked(false, "");
                Bukkit.broadcastMessage(plugin.color("&aSohbet kilidi açıldı!"));
            } else {
                String reason = args.length > 0 ? String.join(" ", args) : "Bakım";
                plugin.setChatLocked(true, reason);
                Bukkit.broadcastMessage(plugin.color("&cSohbet kilitlendi! Sebep: " + reason));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1 && c.getName().equals("chatsustur")) return Collections.singletonList("ac");
        return null;
    }
}