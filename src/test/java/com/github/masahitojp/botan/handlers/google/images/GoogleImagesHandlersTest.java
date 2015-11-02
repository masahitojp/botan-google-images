package com.github.masahitojp.botan.handlers.google.images;

import com.github.masahitojp.botan.Botan;
import com.github.masahitojp.botan.adapter.MockAdapter;
import com.github.masahitojp.botan.brain.LocalBrain;
import com.github.masahitojp.botan.exception.BotanException;
import com.github.masahitojp.botan.utils.HandlersTestUtils;
import com.github.masahitojp.botan.utils.pattern.InvocationRegexPattern;
import com.github.masahitojp.botan.utils.pattern.NotInvocationRegexPattern;
import mockit.Mock;
import mockit.MockUp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GoogleImagesHandlersTest {
    Botan botan;

    @Before
    public void setUp() throws BotanException {
        botan = new Botan.BotanBuilder()
                .setAdapter(new MockAdapter())
                .setBrain(new LocalBrain())
                .setMessageHandlers(new GoogleImagesHandlers())
                .build();
        botan.start();
    }

    @After
    public void tearDown() {
        botan.stop();
    }

    @Test
    public void handlersRegistrationTest() {
        assertThat(botan.getHandlers().size(), is(1));
    }

    @Test
    public void regex() {
        new HandlersTestUtils().regexTest(
                botan,
                Arrays.asList(
                        new InvocationRegexPattern("botan image test"),
                        new InvocationRegexPattern("botan IMAGE test"),
                        new NotInvocationRegexPattern("botan images pingi")
                )
        );
    }

    @Test
    public void imageGet() {

        new MockUp<GoogleImagesHandlers>() {
            @Mock
            final String getBody(final HttpURLConnection conn) throws IOException {
                return "{\"responseData\": {\"results\": [{\"unescapedUrl\": \"url\"}]}}";
            }
        };

        new HandlersTestUtils().replyTest(botan, "botan image test", "url");
    }

}