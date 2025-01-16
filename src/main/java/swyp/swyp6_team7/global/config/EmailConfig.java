package swyp.swyp6_team7.global.config;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import feign.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    private final String apiKey;

    public EmailConfig(
            @Value("${mailgun.apiKey}") String apiKey
    ) {
        this.apiKey = apiKey;
    }

    @Bean
    public MailgunMessagesApi mailgunMessagesApi() {
        return MailgunClient.config(apiKey).logger(new Logger.NoOpLogger()).createApi(MailgunMessagesApi.class);
    }
}
