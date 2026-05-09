package me.toprakbuilds.aechannel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private final AEChannel plugin;
    private final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");

    public LogManager(AEChannel plugin) { this.plugin = plugin; }

    public void log(String type, String sender, String message) {
        try {
            File dir = new File(plugin.getDataFolder(), "logs");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, type + "-" + df.format(new Date()) + ".log");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);

            pw.println("[" + tf.format(new Date()) + "] " + sender + ": " + message);
            pw.flush();
            pw.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}