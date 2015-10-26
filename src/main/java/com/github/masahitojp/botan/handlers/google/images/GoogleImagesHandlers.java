package com.github.masahitojp.botan.handlers.google.images;

import com.github.masahitojp.botan.Robot;
import com.github.masahitojp.botan.handler.BotanMessageHandlers;
import com.github.masahitojp.botan.handlers.google.images.pojo.GoogleImageResponse;
import com.github.masahitojp.botan.utils.BotanUtils;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@Slf4j
@SuppressWarnings("unused")
public class GoogleImagesHandlers implements BotanMessageHandlers {
    private static String ERROR_PREFIX = "Encountered an error : ";

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
                        try (
                                final CloseableHttpClient client = HttpClients.createDefault();
                                final CloseableHttpResponse response = client.execute(new HttpGet(getUri(query)))
                        ) {
                            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                final String src = EntityUtils.toString(response.getEntity());
                                final GoogleImageResponse jsonObject = getGson().fromJson(src, GoogleImageResponse.class);
                                if (jsonObject != null && jsonObject.responseData != null && jsonObject.responseData.results.size() > 0) {
                                    final String imageUrl = BotanUtils.getRandomValue(jsonObject.responseData.results).unescapedUrl;
                                    message.reply(imageUrl);
                                } else {
                                    message.reply(String.format("Sorry, I found no results for '%s'.", query));
                                }
                            } else {
                                message.reply(ERROR_PREFIX + String.format("http request failed (%d)", response.getStatusLine().getStatusCode()));
                            }
                        }
                    } catch (final URISyntaxException | IOException e) {
                        log.warn(e.getMessage());
                        message.reply(ERROR_PREFIX + e.getMessage());
                    }
                }
        );

    }

    private URI getUri(String query) throws URISyntaxException {
        final String GOOGLE_IMAGE_API_URL = "ajax.googleapis.com";
        return new URIBuilder()
                .setScheme("http")
                .setHost(GOOGLE_IMAGE_API_URL)
                .setPath("/ajax/services/search/images")
                .addParameter("q", query)
                .addParameter("rsz", "8")
                .addParameter("safe", "active")
                .addParameter("v", "1.0")
                .build();
    }
}