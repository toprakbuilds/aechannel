package me.toprakbuilds.aechannel;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class AEChannel extends JavaPlugin {
    private final Map<UUID, PlayerSettings> settingsMap = new HashMap<>();
    private List<String> bannedWords = new ArrayList<>();
    private DatabaseManager db;
    private LogManager logManager;
    private boolean chatLocked = false;
    private String lockReason = "";

    private File lpChatFile;
    private FileConfiguration lpChatConfig;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    private File discordFile;
    private FileConfiguration discordConfig;

    @Override
    public void onEnable() {
        // 1. Yapılandırmalar
        saveDefaultConfig();
        loadLPChatConfig();
        loadMessagesConfig();
        loadDiscordConfig(); // Discord yapılandırması eklendi
        loadBannedWords();

        // 2. Sistemler
        db = new DatabaseManager(this);
        logManager = new LogManager(this);

        // 3. Eventler
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        registerJoinQuitEvents();

        // 4. Komut Kayıtları
        ModCommands modCmd = new ModCommands(this);
        CommandHandler commonCmd = new CommandHandler(this);

        setupCommand("sustur", modCmd);
        setupCommand("sustur-ac", modCmd);
        setupCommand("chatsustur", modCmd);
        setupCommand("all-global", commonCmd);
        setupCommand("globalmesaj", commonCmd);
        setupCommand("ticaretmesaj", commonCmd);
        setupCommand("msg", commonCmd);
        setupCommand("reply", commonCmd);

        setupCommand("aereload", (sender, cmd, label, args) -> {
            if (!sender.hasPermission("aechannel.admin.reload")) return true;
            reloadAllConfigs();
            sender.sendMessage(color(getMessagesConfig().getString("reload", "&aEklenti başarıyla yenilendi!")));
            return true;
        });

        // 5. Duyuru Sistemi
        int interval = getConfig().getInt("announcements.interval", 300) * 20;
        if (interval > 0) {
            new AutoBroadcast(this).runTaskTimer(this, interval, interval);
        }

        getLogger().info("AEChannel v2.2.0 aktif! Atlas için tüm özellikler devrede.");
    }

    private void setupCommand(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("UYARI: '" + name + "' komutu plugin.yml içinde eksik!");
        }
    }

    private void registerJoinQuitEvents() {
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                Player p = e.getPlayer();
                PlayerSettings s = new PlayerSettings();
                db.load(p.getUniqueId(), s);
                s.checkReset();
                settingsMap.put(p.getUniqueId(), s);
                logManager.log("connection", p.getName(), "Sunucuya katıldı.");
            }
            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
                Player p = e.getPlayer();
                if (settingsMap.containsKey(p.getUniqueId())) {
                    db.save(p.getUniqueId(), settingsMap.get(p.getUniqueId()));
                    settingsMap.remove(p.getUniqueId());
                }
            }
        }, this);
    }

    public void loadDiscordConfig() {
        discordFile = new File(getDataFolder(), "discord.yml");
        if (!discordFile.exists()) saveResource("discord.yml", false);
        discordConfig = YamlConfiguration.loadConfiguration(discordFile);
    }

    public void loadLPChatConfig() {
        lpChatFile = new File(getDataFolder(), "lp-chat.yml");
        if (!lpChatFile.exists()) saveResource("lp-chat.yml", false);
        lpChatConfig = YamlConfiguration.loadConfiguration(lpChatFile);
    }

    public void loadMessagesConfig() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void loadBannedWords() {
        File file = new File(getDataFolder(), "kufur.txt");
        try {
            if (!file.exists()) {
                getDataFolder().mkdirs();
                Files.write(file.toPath(), Collections.singletonList("kufur1"));
            }
            bannedWords = Files.readAllLines(file.toPath());
        } catch (Exception e) {
            bannedWords = new ArrayList<>();
        }
    }

    public void reloadAllConfigs() {
        reloadConfig();
        loadLPChatConfig();
        loadMessagesConfig();
        loadDiscordConfig();
        loadBannedWords();
    }

    public FileConfiguration getDiscordConfig() { return discordConfig; }
    public FileConfiguration getLPChatConfig() { return lpChatConfig; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public List<String> getBannedWords() { return bannedWords; }
    public PlayerSettings getSettings(Player p) {
        return settingsMap.getOrDefault(p.getUniqueId(), new PlayerSettings());
    }
    public DatabaseManager getDb() { return db; }
    public LogManager getLogManager() { return logManager; }
    public boolean isChatLocked() { return chatLocked; }
    public String getLockReason() { return lockReason; }
    public void setChatLocked(boolean l, String r) { this.chatLocked = l; this.lockReason = r; }

    public String color(String m) {
        return m == null ? "" : ChatColor.translateAlternateColorCodes('&', m);
    }

    public boolean isInRegion(Player p, String id) {
        try {
            RegionQuery q = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            return q.getApplicableRegions(BukkitAdapter.adapt(p.getLocation())).getRegions().stream()
                    .anyMatch(r -> r.getId().equalsIgnoreCase(id));
        } catch (Exception e) { return false; }
    }
}