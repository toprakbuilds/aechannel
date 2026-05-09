package me.toprakbuilds.aechannel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    // Atlas hocam, send metodunu senin ModCommands'te kullandığın gibi (String, String) yapısına uygun hale getirdim
    public static void send(String urlString, String content) {
        if (urlString == null || urlString.isEmpty() || urlString.equals("WEBHOOK_LINK_BURAYA")) return;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-Atlas");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            // Basit JSON formatı: {"content": "mesaj"}
            JSONObject json = new JSONObject();
            json.put("content", content);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            connection.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}