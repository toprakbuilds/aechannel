package me.toprakbuilds.aechannel;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    private final AEChannel plugin;
    public ChatListener(AEChannel plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        PlayerSettings s = plugin.getSettings(p);
        String msg = e.getMessage();

        // Sohbet Kilidi & Mute Kontrolü
        if (plugin.isChatLocked() && !p.hasPermission("aechannel.bypass.lock")) {
            p.sendMessage(plugin.color(plugin.getMessagesConfig().getString("chat-locked").replace("%reason%", plugin.getLockReason())));
            e.setCancelled(true); return;
        }
        if (s.isMuted()) {
            p.sendMessage(plugin.color(plugin.getMessagesConfig().getString("player-muted").replace("%seconds%", String.valueOf((s.muteEnd - System.currentTimeMillis()) / 1000))));
            e.setCancelled(true); return;
        }

        // Spam & Cooldown
        long now = System.currentTimeMillis();
        if (now - s.lastMessageTime < plugin.getConfig().getLong("settings.chat-cooldown") && !p.hasPermission("aechannel.bypass.cooldown")) {
            p.sendMessage(plugin.color(plugin.getMessagesConfig().getString("spam-warn")));
            e.setCancelled(true); return;
        }

        // Filtreler (Unicode, Caps, Gramer)
        if (plugin.getConfig().getBoolean("settings.anti-unicode") && !p.hasPermission("aechannel.bypass.unicode")) {
            if (!msg.chars().allMatch(c -> c < 128 || "ğüşıöçĞÜŞİÖÇ".indexOf(c) >= 0)) {
                p.sendMessage(plugin.color(plugin.getMessagesConfig().getString("unicode-warn")));
                e.setCancelled(true); return;
            }
        }

        // Küfür Filtresi
        boolean hasBadWord = false;
        for (String word : plugin.getBannedWords()) {
            if (!word.isEmpty() && msg.toLowerCase().contains(word.toLowerCase())) {
                msg = msg.replaceAll("(?i)" + Pattern.quote(word), "***");
                hasBadWord = true;
            }
        }
        if (hasBadWord) applyPenalty(p, s);

        s.lastMessage = msg;
        s.lastMessageTime = now;
        e.setCancelled(true);

        // --- MENTION SİSTEMİ ---
        handleMentions(p, msg);

        // --- DISCORD LOGLAMA ---
        if (plugin.getConfig().getBoolean("discord-log-enabled")) {
            String webhook = plugin.getDiscordConfig().getString("channels.chat");
            String format = plugin.getDiscordConfig().getString("messages.chat-format")
                    .replace("%player%", p.getName())
                    .replace("%message%", msg);
            DiscordWebhook.send(webhook, format);
        }

        // --- MESAJ DAĞITIM ---
        TextComponent finalJSON = createJSONMessage(p, msg);
        if (msg.startsWith(".")) {
            broadcastJSON(finalJSON, "trade", plugin.getConfig().getString("tags.ticaret"));
        } else if (msg.startsWith("!") || s.allGlobal) {
            broadcastJSON(finalJSON, "global", plugin.getConfig().getString("tags.global"));
        } else {
            handleLocalJSON(p, finalJSON);
        }
    }

    private void handleMentions(Player p, String msg) {
        boolean requiresAt = plugin.getConfig().getBoolean("settings.mention-requires-at", false);
        for (Player target : Bukkit.getOnlinePlayers()) {
            String name = target.getName();
            boolean isMentioned = requiresAt ? msg.contains("@" + name) : msg.toLowerCase().contains(name.toLowerCase());

            if (isMentioned) {
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                target.sendMessage(plugin.color("&a&l[!] &e" + p.getName() + " &7senden bahsetti!"));
            }
        }
    }

    private TextComponent createJSONMessage(Player p, String message) {
        String prefix = "";
        String group = "default";
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(p.getUniqueId());
            if (user != null) {
                prefix = user.getCachedData().getMetaData().getPrefix() != null ? user.getCachedData().getMetaData().getPrefix() : "";
                group = user.getPrimaryGroup();
            }
        }

        String finalContent = message;
        for(Player online : Bukkit.getOnlinePlayers()) {
            if(message.toLowerCase().contains(online.getName().toLowerCase())) {
                finalContent = "&a" + message;
                break;
            }
        }

        FileConfiguration lpc = plugin.getLPChatConfig();
        String format = lpc.getString("groups." + group + ".format", "&7%prefix%%player%&8: &f%message%");
        String fullText = plugin.color(format.replace("%prefix%", prefix).replace("%player%", p.getName()).replace("%message%", finalContent));

        TextComponent textPart = new TextComponent(fullText);
        textPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.color(lpc.getString("groups." + group + ".hover")))));
        textPart.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + p.getName() + " "));
        return textPart;
    }

    private void handleLocalJSON(Player p, TextComponent msg) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld().equals(p.getWorld()) && target.getLocation().distance(p.getLocation()) <= plugin.getConfig().getDouble("settings.proximity-range")) {
                target.spigot().sendMessage(msg);
            }
        }
    }

    private void broadcastJSON(TextComponent msg, String type, String tag) {
        TextComponent withTag = new TextComponent(plugin.color(tag) + " ");
        withTag.addExtra(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerSettings s = plugin.getSettings(p);
            if ((type.equals("global") && s.seeGlobal) || (type.equals("trade") && s.seeTrade)) {
                p.spigot().sendMessage(withTag);
            }
        }
    }

    private void applyPenalty(Player p, PlayerSettings s) {
        s.checkReset();
        s.warnCount++;
        p.sendMessage(plugin.color(plugin.getMessagesConfig().getString("kufur-warn-" + (s.warnCount >= 4 ? "final" : s.warnCount))));

        if (s.warnCount == 2) s.muteEnd = System.currentTimeMillis() + 60000L;
        if (s.warnCount == 3) s.muteEnd = System.currentTimeMillis() + 600000L;
        if (s.warnCount >= 4) s.muteEnd = System.currentTimeMillis() + 3600000L;

        if (plugin.getConfig().getBoolean("discord-log-enabled")) {
            DiscordWebhook.send(plugin.getDiscordConfig().getString("channels.warn"), "⚠️ **[WARN]** " + p.getName() + " otomatik uyarıldı. Uyarı: " + s.warnCount);
        }
        plugin.getDb().save(p.getUniqueId(), s);
    }
}