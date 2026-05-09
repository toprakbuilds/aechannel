package me.toprakbuilds.aechannel;

public class PlayerSettings {
    // Veritabanı ve Kanal Ayarları
    public boolean allGlobal = false; // İsmi bu şekilde sabitledik
    public boolean seeGlobal = true;
    public boolean seeTrade = true;

    // Ceza Ayarları
    public long muteEnd = 0;
    public String muteReason = "";
    public int warnCount = 0;
    public long lastWarnTime = 0;

    // Spam ve Tekrar Koruması Verileri
    public long lastMessageTime = 0;
    public String lastMessage = "";

    // Yardımcı Metotlar
    public boolean isMuted() {
        return muteEnd > System.currentTimeMillis();
    }

    public long getMuteSeconds() {
        return Math.max(0, (muteEnd - System.currentTimeMillis()) / 1000);
    }

    public void checkReset() {
        if (System.currentTimeMillis() - lastWarnTime > 86400000L) {
            warnCount = 0;
        }
        if (!isMuted()) {
            muteReason = "";
        }
    }
}