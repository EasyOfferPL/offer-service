package pl.easyoffer.offer_service.client.theprotocolit.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.client.theprotocolit.TheProtocolClient;
import pl.easyoffer.offer_service.client.theprotocolit.TheProtocolClientManager;
import pl.easyoffer.offer_service.model.dto.theprotocolit.TheProtocolOffer;
import pl.easyoffer.offer_service.model.dto.theprotocolit.TheProtocolOfferData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TheProtocolClientImpl implements TheProtocolClient {

    private static final String WEBSITE_URL =
            "https://theprotocol.it/filtry/java;t?sort=date";

    private static final String API_URL =
            "https://apus-api.theprotocol.it/offers/_search" +
                    "?pageNumber=%s" +
                    "&orderby.field=LastRefreshDateUtc" +
                    "&pageSize=%s";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/135.0.0.0 Safari/537.36";

    private static final String FETCH_SCRIPT = """
        async (config) => {
            const response = await fetch(config.apiUrl, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'accept': 'application/json, text/plain, */*',
                    'content-type': 'application/json',
                    'origin': 'https://theprotocol.it',
                    'referer': 'https://theprotocol.it/',
                    'x-application-name': 'theprotocol-offers',
                    'x-application-version': '4.4.1309',
                    'x-xsrf-token': config.xsrfToken
                },
                body: JSON.stringify({
                    expectedTechnologies: ["%s"]
                })
            });

            if (!response.ok) {
                throw new Error(
                    `HTTP ${response.status}: ${await response.text()}`
                );
            }

            return await response.text();
        }
        """;

    private final TheProtocolClientManager theProtocolClientManager;
    private final ObjectMapper objectMapper;

    @Override
    public List<TheProtocolOffer> getAllOffers(List<String> categories) {
        try (Browser browser = theProtocolClientManager.newBrowser();
             BrowserContext context = createContext(browser)) {
            Page page = context.newPage();
            openOffersPage(page);
            String xsrfToken = extractXsrfToken(context);
            List<TheProtocolOffer> theProtocolOffers = new ArrayList<>();
            for (String category : categories) {
                theProtocolOffers.addAll(retrieveCategoryOffers(category, page, xsrfToken));
            }
            return theProtocolOffers;
        } catch (Exception e) {
            log.error("Failed to fetch offers from TheProtocol.it", e);
            throw new IllegalStateException("Could not synchronize TheProtocol.it offers", e);
        }
    }

    private BrowserContext createContext(Browser browser) {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setUserAgent(USER_AGENT)
                .setLocale("en-US");
        return browser.newContext(contextOptions);
    }

    private void openOffersPage(Page page) {
        Page.NavigateOptions navigateOptions = new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(60_000);
        page.navigate(WEBSITE_URL, navigateOptions);
        page.waitForTimeout(10_000);
    }

    private String extractXsrfToken(BrowserContext context) {
        return context.cookies().stream()
                .filter(cookie -> "XSRF-TOKEN".equals(cookie.name))
                .findFirst()
                .map(cookie -> cookie.value)
                .orElseThrow(() -> new IllegalStateException("XSRF-TOKEN cookie was not found"));
    }

    private List<TheProtocolOffer> retrieveCategoryOffers(String category, Page browserPage, String xsrfToken)
            throws JsonProcessingException {
        int pageSize = 50;
        int pageNumber = 1;

        List<TheProtocolOffer> offers = new ArrayList<>();
        TheProtocolOfferData offerData;
        do {
            String apiUrl = API_URL.formatted(pageNumber, pageSize);
            String fetchScript = FETCH_SCRIPT.formatted(category);
            log.info("Request for {} category: {}", category, apiUrl);
            String response = performRequest(browserPage, apiUrl, fetchScript, xsrfToken);
            offerData = objectMapper.readValue(response, TheProtocolOfferData.class);
            offers.addAll(offerData.getOffers());
            pageNumber++;
            log.info("Response for {} category: {}", category, response);
        } while (offerData.getOffers().size() == pageSize);
        return offers;
    }

    private String performRequest(Page browserPage, String apiUrl, String fetchScript, String xsrfToken) {
        Map<String, String> requestArguments = Map.of(
                "apiUrl", apiUrl,
                "xsrfToken", xsrfToken
        );
        return browserPage.evaluate(fetchScript, requestArguments).toString();
    }

}
