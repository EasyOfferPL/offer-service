package pl.easyoffer.offer_service.client.theprotocolit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

public class TheProtocolBrowserSession implements AutoCloseable {

    private final Playwright playwright;
    private final Browser browser;

    TheProtocolBrowserSession(Playwright playwright, Browser browser) {
        this.playwright = playwright;
        this.browser = browser;
    }

    public Browser browser() {
        return browser;
    }

    @Override
    public void close() {
        try {
            browser.close();
        } finally {
            playwright.close();
        }
    }
}
