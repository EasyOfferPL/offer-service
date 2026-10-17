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

    public TheProtocolBrowserSession openSession() {
        Playwright playwright = Playwright.create();
        try {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(ADDITIONAL_ARGUMENTS)
            );
            return new TheProtocolBrowserSession(playwright, browser);
        } catch (RuntimeException exception) {
            closeAfterFailedLaunch(playwright, exception);
            throw exception;
        }
    }

    private void closeAfterFailedLaunch(Playwright playwright, RuntimeException launchException) {
        try {
            playwright.close();
        } catch (RuntimeException closeException) {
            launchException.addSuppressed(closeException);
        }
    }
}
