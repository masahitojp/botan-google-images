package com.github.masahitojp.botan.handlers.google.images;

import com.github.masahitojp.botan.Robot;
import com.github.masahitojp.botan.handler.BotanMessageHandlers;
import com.github.masahitojp.botan.handlers.google.images.pojo.GoogleImageResponse;
import com.github.masahitojp.botan.utils.BotanUtils;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


@Slf4j
@SuppressWarnings("unused")
public class GoogleImagesHandlers implements BotanMessageHandlers {
    private static String ERROR_PREFIX = "Encountered an error : ";
    private static String GOOGLE_IMAGE_API_URL = "https://www.googleapis.com/customsearch/v1";

    private String id;
    private String key;

    public GoogleImagesHandlers() {
        id = BotanUtils.envToOpt("GOOGLE_CSE_ID").orElse("");
        key = BotanUtils.envToOpt("GOOGLE_CSE_KEY").orElse("");
    }

    @Getter(lazy = true)
    private final Gson gson = initializeGson();

    private Gson initializeGson() {
        return new Gson();
    }

    @Override
    public void register(final Robot robot) {
        robot.respond(
                "(?i)(image|img) (?<query>.+)\\z",
                "Queries Google Images for <query> and returns a random top result.",
                message -> {
                    final String query = message.getMatcher().group("query");
                    try {
                        final String q = URLEncoder.encode(query, "UTF-8");
                        final String fields = URLEncoder.encode("items(link)", "UTF-8");
                        final String uid = URLEncoder.encode(id, "UTF-8");
                        final String s= GOOGLE_IMAGE_API_URL + String.format(
                                "?q=%s&searchType=image&safe=high&fields=%s&cx=%s&key=%s",
                                q, fields, uid, key);

                        final URL url = new URL(s);
                        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        final int code = conn.getResponseCode();

                        if (code == 200) {
                            final String src = getBody(conn);
                            final GoogleImageResponse jsonObject = getGson().fromJson(src, GoogleImageResponse.class);
                            if (jsonObject != null && jsonObject.items != null && jsonObject.getItems().size() > 0) {
                                final String imageUrl = BotanUtils.getRandomValue(jsonObject.getItems()).link;
                                message.reply(imageUrl);
                            } else {
                                message.reply(String.format("Sorry, I found no results for '%s'.", query));
                            }
                        } else {
                            message.reply(ERROR_PREFIX + String.format("http request failed (%d)", code));
                        }

                    } catch (final IOException e) {
                        log.warn(e.getMessage());
                        message.reply(ERROR_PREFIX + e.getMessage());
                    }
                }
        );

    }

    private String getBody(final HttpURLConnection conn) throws IOException {
        final StringBuilder builder = new StringBuilder();
        try (final ReadableByteChannel channel = Channels.newChannel(conn.getInputStream())) {
            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            int noOfBytes;
            byte[] data;
            while ((noOfBytes = channel.read(buffer)) > 0) {
                data = new byte[noOfBytes];
                System.arraycopy(buffer.array(), 0, data, 0, noOfBytes);
                buffer.clear();
                builder.append(new String(data));
            }
        }
        return builder.toString();
    }

}