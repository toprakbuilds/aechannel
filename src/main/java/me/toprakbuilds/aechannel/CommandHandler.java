package me.toprakbuilds.aechannel;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;
import java.util.Arrays;

public class CommandHandler implements CommandExecutor {
    private final AEChannel plugin;
    private final HashMap<UUID, UUID> lastMessaged = new HashMap<>();

    public CommandHandler(AEChannel plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String cmdName = cmd.getName().toLowerCase();

        // Alias Kontrolü
        if (plugin.getConfig().getConfigurationSection("aliases") != null) {
            for (String alias : plugin.getConfig().getConfigurationSection("aliases").getKeys(false)) {
                if (label.equalsIgnoreCase(alias)) {
                    cmdName = plugin.getConfig().getString("aliases." + alias).toLowerCase();
                    break;
                }
            }
        }

        switch (cmdName) {
            case "msg" -> {
                if (args.length < 2) { p.sendMessage(plugin.color("&cKullanım: /msg <oyuncu> <mesaj>")); return true; }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) { p.sendMessage(plugin.color("&cOyuncu bulunamadı.")); return true; }
                sendPrivateMessage(p, target, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            }
            case "reply" -> {
                UUID last = lastMessaged.get(p.getUniqueId());
                if (last == null || Bukkit.getPlayer(last) == null) { p.sendMessage(plugin.color("&cMesaj atacak kimse yok.")); return true; }
                if (args.length < 1) { p.sendMessage(plugin.color("&cKullanım: /reply <mesaj>")); return true; }
                sendPrivateMessage(p, Bukkit.getPlayer(last), String.join(" ", args));
            }
            case "globalmesaj" -> {
                if (args.length < 1) return true;
                Bukkit.broadcastMessage(plugin.color("&8[&bGLOBAL&8] &f" + p.getName() + ": &7" + String.join(" ", args)));
            }
            case "ticaretmesaj" -> {
                if (args.length < 1) return true;
                Bukkit.broadcastMessage(plugin.color("&8[&6TİCARET&8] &e" + p.getName() + ": &f" + String.join(" ", args)));
            }
            case "all-global" -> {
                PlayerSettings s = plugin.getSettings(p);
                s.allGlobal = !s.allGlobal; // DÜZELTME: PlayerSettings'deki isimle eşitlendi
                p.sendMessage(plugin.color("&8[&bAE&8] &7Global mod: " + (s.allGlobal ? "&aAçık" : "&cKapalı")));
            }
        }
        return true;
    }

    private void sendPrivateMessage(Player from, Player to, String msg) {
        String formatTo = plugin.getMessagesConfig().getString("pm-format-to", "&8[&bBen &3-> &b%player%&8] &f%message%");
        String formatFrom = plugin.getMessagesConfig().getString("pm-format-from", "&8[&b%player% &3-> &bBen&8] &f%message%");

        from.sendMessage(plugin.color(formatTo.replace("%player%", to.getName()).replace("%message%", msg)));
        to.sendMessage(plugin.color(formatFrom.replace("%player%", from.getName()).replace("%message%", msg)));

        to.playSound(to.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);

        lastMessaged.put(from.getUniqueId(), to.getUniqueId());
        lastMessaged.put(to.getUniqueId(), from.getUniqueId());
    }
}