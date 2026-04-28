package pl.easyoffer.offer_service.client.theprotocolit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TheProtocolClientManager {

    private static final List<String> ADDITIONAL_ARGUMENTS = List.of(
            "--no-sandbox",
            "--disable-setuid-sandbox",
            "--disable-dev-shm-usage"
    );

    private final Playwright playwright;
    private final Browser browser;

    public TheProtocolClientManager() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(ADDITIONAL_ARGUMENTS)
        );
    }

    public Browser newBrowser() {
        return browser;
    }
}