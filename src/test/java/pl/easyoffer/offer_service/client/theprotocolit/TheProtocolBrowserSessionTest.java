package pl.easyoffer.offer_service.client.theprotocolit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TheProtocolBrowserSessionTest {

    @Test
    void shouldCloseBrowserBeforePlaywright() {
        Playwright playwright = mock(Playwright.class);
        Browser browser = mock(Browser.class);
        TheProtocolBrowserSession session = new TheProtocolBrowserSession(playwright, browser);

        session.close();

        InOrder closeOrder = inOrder(browser, playwright);
        closeOrder.verify(browser).close();
        closeOrder.verify(playwright).close();
    }

    @Test
    void shouldClosePlaywrightWhenBrowserCloseFails() {
        Playwright playwright = mock(Playwright.class);
        Browser browser = mock(Browser.class);
        TheProtocolBrowserSession session = new TheProtocolBrowserSession(playwright, browser);
        PlaywrightException closeFailure = new PlaywrightException("Browser close failed");
        doThrow(closeFailure).when(browser).close();

        assertThatThrownBy(session::close).isSameAs(closeFailure);

        verify(playwright).close();
    }
}
