package me.toprakbuilds.aechannel;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;

public class CommandHandler implements CommandExecutor {
    private final AEChannel plugin;
    private final HashMap<UUID, UUID> lastMessaged = new HashMap<>();

    public CommandHandler(AEChannel plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        switch (cmd.getName().toLowerCase()) {
            case "msg" -> {
                if (args.length < 2) return false;
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) { p.sendMessage(plugin.color("&cOyuncu bulunamadı.")); return true; }

                String msg = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                sendPrivateMessage(p, target, msg);
            }
            case "reply" -> {
                if (args.length < 1) return false;
                UUID last = lastMessaged.get(p.getUniqueId());
                if (last == null || Bukkit.getPlayer(last) == null) { p.sendMessage(plugin.color("&cMesaj atacak kimse yok.")); return true; }

                sendPrivateMessage(p, Bukkit.getPlayer(last), String.join(" ", args));
            }
        }
        return true;
    }

    private void sendPrivateMessage(Player from, Player to, String msg) {
        // DÜZELTME: plugin.getMessagesConfig() kullanıldı.
        String formatTo = plugin.getMessagesConfig().getString("pm-format-to", "&8[&bBen &3-> &b%player%&8] &f%message%");
        String formatFrom = plugin.getMessagesConfig().getString("pm-format-from", "&8[&b%player% &3-> &bBen&8] &f%message%");

        from.sendMessage(plugin.color(formatTo.replace("%player%", to.getName()).replace("%message%", msg)));
        to.sendMessage(plugin.color(formatFrom.replace("%player%", from.getName()).replace("%message%", msg)));

        // Mesaj gelince "trink" sesi
        to.playSound(to.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);

        lastMessaged.put(from.getUniqueId(), to.getUniqueId());
        lastMessaged.put(to.getUniqueId(), from.getUniqueId());

        plugin.getLogManager().log("private-message", from.getName() + " -> " + to.getName(), msg);
    }
}