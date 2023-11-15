package yeoksamstationexit1.usermanage.global.webClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class webClientUtil {


    @Value("${backenduri}")
    private String backenduri;


    @Bean
    public WebClient webClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(backenduri)
                .defaultHeader("Content-Type", "application/json")
                .build();
        return webClient;
    }



}
