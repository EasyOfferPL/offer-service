package pl.easyoffer.offer_service.client.theprotocolit.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.easyoffer.offer_service.client.theprotocolit.TheProtocolBrowserSession;
import pl.easyoffer.offer_service.client.theprotocolit.TheProtocolClientManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TheProtocolClientImplTest {

    private static final String EMPTY_OFFERS_RESPONSE = "{\"offers\":[]}";

    @Mock
    private TheProtocolClientManager clientManager;

    private TheProtocolClientImpl client;

    @BeforeEach
    void setUp() {
        client = new TheProtocolClientImpl(clientManager, new ObjectMapper());
    }

    @Test
    void shouldUseFreshBrowserSessionForEachSynchronization() {
        TheProtocolBrowserSession firstSession = successfulSession();
        TheProtocolBrowserSession secondSession = successfulSession();
        when(clientManager.openSession()).thenReturn(firstSession, secondSession);

        assertThat(client.getAllOffers(List.of("java"))).isEmpty();
        assertThat(client.getAllOffers(List.of("java"))).isEmpty();

        verify(clientManager, times(2)).openSession();
        verify(firstSession).close();
        verify(secondSession).close();
    }

    @Test
    void shouldRetryOnceWithFreshSessionAfterBrowserFailure() {
        TheProtocolBrowserSession failedSession = mock(TheProtocolBrowserSession.class);
        Browser failedBrowser = mock(Browser.class);
        when(failedSession.browser()).thenReturn(failedBrowser);
        when(failedBrowser.newContext(any(Browser.NewContextOptions.class)))
                .thenThrow(new PlaywrightException("Target has been closed"));

        TheProtocolBrowserSession successfulSession = successfulSession();
        when(clientManager.openSession()).thenReturn(failedSession, successfulSession);

        assertThat(client.getAllOffers(List.of("java"))).isEmpty();

        verify(clientManager, times(2)).openSession();
        verify(failedSession).close();
        verify(successfulSession).close();
    }

    @Test
    void shouldStopAfterSecondBrowserFailure() {
        PlaywrightException firstFailure = new PlaywrightException("First browser failed");
        PlaywrightException secondFailure = new PlaywrightException("Second browser failed");
        when(clientManager.openSession()).thenThrow(firstFailure, secondFailure);

        assertThatThrownBy(() -> client.getAllOffers(List.of("java")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not synchronize TheProtocol.it offers")
                .hasCause(secondFailure);

        verify(clientManager, times(2)).openSession();
    }

    private TheProtocolBrowserSession successfulSession() {
        TheProtocolBrowserSession session = mock(TheProtocolBrowserSession.class);
        Browser browser = mock(Browser.class);
        BrowserContext context = mock(BrowserContext.class);
        Page page = mock(Page.class);

        when(session.browser()).thenReturn(browser);
        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(context);
        when(context.newPage()).thenReturn(page);
        when(context.cookies()).thenReturn(List.of(new Cookie("XSRF-TOKEN", "token")));
        when(page.evaluate(anyString(), any())).thenReturn(EMPTY_OFFERS_RESPONSE);
        return session;
    }
}
