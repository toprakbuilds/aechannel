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
        if (!sender.hasPermission("aechannel.moderator")) {
            sender.sendMessage(plugin.color("&cBu komut için yetkin yok!"));
            return true;
        }

        // --- ALIAS SİSTEMİ BAŞLANGIÇ ---
        String cmdName = cmd.getName().toLowerCase();
        // Config içindeki 'aliases' bölümünü kontrol eder, eğer label bir takma ad ise ana komuta çevirir.
        if (plugin.getConfig().getConfigurationSection("aliases") != null) {
            for (String alias : plugin.getConfig().getConfigurationSection("aliases").getKeys(false)) {
                if (label.equalsIgnoreCase(alias)) {
                    cmdName = plugin.getConfig().getString("aliases." + alias).toLowerCase();
                    break;
                }
            }
        }
        // --- ALIAS SİSTEMİ BİTİŞ ---

        if (cmdName.equals("sustur")) {
            if (args.length < 3) { sender.sendMessage(plugin.color("&cKullanım: /sustur <oyuncu> <saniye> <neden>")); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage(plugin.color("&cOyuncu aktif değil.")); return true; }

            long duration = Long.parseLong(args[1]);
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            PlayerSettings s = plugin.getSettings(target);
            s.muteEnd = System.currentTimeMillis() + (duration * 1000L);
            s.muteReason = reason;
            plugin.getDb().save(target.getUniqueId(), s);

            sender.sendMessage(plugin.color("&a" + target.getName() + " susturuldu."));
            target.sendMessage(plugin.color("&c" + reason + " sebebiyle susturuldun!"));

            if (plugin.getConfig().getBoolean("discord-log-enabled")) {
                DiscordWebhook.send(plugin.getDiscordConfig().getString("channels.mute"), "🚫 **[MUTE]** " + sender.getName() + " -> " + target.getName() + " | Sebep: " + reason);
            }
        }
        else if (cmdName.equals("sustur-ac")) {
            if (args.length < 1) { sender.sendMessage(plugin.color("&cKullanım: /sustur-ac <oyuncu>")); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage(plugin.color("&cOyuncu bulunamadı.")); return true; }

            PlayerSettings s = plugin.getSettings(target);
            s.muteEnd = 0;
            plugin.getDb().save(target.getUniqueId(), s);
            sender.sendMessage(plugin.color("&a" + target.getName() + " kişisinin cezası kaldırıldı."));
            target.sendMessage(plugin.color("&aSusturman kaldırıldı, artık konuşabilirsin."));

            if (plugin.getConfig().getBoolean("discord-log-enabled")) {
                DiscordWebhook.send(plugin.getDiscordConfig().getString("channels.mute"), "🔓 **[UNMUTE]** " + sender.getName() + " -> " + target.getName());
            }
        }
        else if (cmdName.equals("chatsustur")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("ac")) {
                plugin.setChatLocked(false, "");
                Bukkit.broadcastMessage(plugin.color("&aSohbet kilidi açıldı!"));
            } else {
                String reason = args.length > 0 ? String.join(" ", args) : "Bakım";
                plugin.setChatLocked(true, reason);
                Bukkit.broadcastMessage(plugin.color("&cSohbet kilitlendi! Sebep: " + reason));
            }
        }
        else if (cmdName.equals("aereload")) {
            plugin.reloadConfig();
            sender.sendMessage(plugin.color("&aAEChannel ayarları yenilendi!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1 && c.getName().equalsIgnoreCase("chatsustur")) return Collections.singletonList("ac");
        return null;
    }
}