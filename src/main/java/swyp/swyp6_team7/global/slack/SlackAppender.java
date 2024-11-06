package swyp.swyp6_team7.global.slack;


import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SlackAppender extends AppenderBase<ILoggingEvent> {
    private WebClient webClient = WebClient.create();
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String message = eventObject.getFormattedMessage();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(eventObject.getTimeStamp()));


        if (url == null || url.isEmpty()) {
            return;
        }

        String jsonMessage = "{"
                + "\"blocks\": ["
                + "    {\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \":red_circle: *Error Detect*\"}},"
                + "    {\"type\": \"divider\"},"
                + "    {\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \"*Timestamp*\\n" + timestamp + "\"}},"
                + "    {\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \"*Exception Class*\\n"
                + eventObject.getThrowableProxy().getClassName() + "\"}},"
                + "    {\"type\": \"section\", \"text\": {\"type\": \"mrkdwn\", \"text\": \"*Message*\\n"
                + message + "\"}}"
                + "]}";


        webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(jsonMessage)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
