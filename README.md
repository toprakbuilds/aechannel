```src/main/java/me/toprakbuilds/aechannel/
├── AEChannel.java         <-- Ana sınıf, eklentinin kalbi ve başlatıcı.
├── AutoBroadcast.java     <-- Belirli aralıklarla otomatik duyuru yapan sistem.
├── ChatListener.java      <-- Sohbeti dinleyen, filtreleyen ve mentionları yöneten kısım.
├── CommandHandler.java    <-- Oyuncu komutlarını (msg, global vb.) işleyen sınıf.
├── DatabaseManager.java   <-- Oyuncu verilerinin kaydedilmesi ve yüklenmesi.
├── DiscordWebhook.java    <-- Discord sunucusuna veri gönderen köprü.
├── Formatter.java         <-- Mesajların renklerini ve JSON formatlarını düzenler.
├── LogManager.java        <-- Sunucu içi olayları (katılma/ayrılma) kaydeden sistem.
├── ModCommands.java       <-- Yetkili komutları (sustur, chatsustur vb.).
└── PlayerSettings.java    <-- Her oyuncunun kişisel tercihlerini tutan veri modeli.

plugins/AEChannel/
├── config.yml        <-- (Genel ayarlar / General settings)
├── messages.yml      <-- (Mesajlar ve diller / Messages and locales)
├── discord.yml       <-- (Webhook ve log ayarları / Discord settings)
├── lp-chat.yml       <-- (Grup ve format ayarları / LP Chat formats)
├── kufur.txt         <-- (Filtrelenecek kelimeler / Banned words)
└── data.db           <-- (Oyuncu verileri / Player database)

herhangi bir sorun olursa www.TOPRAKATES.com.tr'den ulasabilirsiniz.```
