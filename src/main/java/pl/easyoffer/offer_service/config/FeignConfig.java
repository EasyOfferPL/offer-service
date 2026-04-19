package pl.easyoffer.offer_service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pl.easyoffer.offer_service.client.JustJoinItClient;

@Configuration
public class FeignConfig {

    @Bean
    public JustJoinItClient jobOfferClient(
            @Value("${service.justJoinIt-service}") String url
    ) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return Feign.builder()
                .encoder(new JacksonEncoder(mapper))
                .decoder(new JacksonDecoder(mapper))
                .logLevel(Logger.Level.BASIC)
                .target(JustJoinItClient.class, url);
    }
}