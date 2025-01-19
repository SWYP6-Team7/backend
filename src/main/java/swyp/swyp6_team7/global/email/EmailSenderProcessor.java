package swyp.swyp6_team7.global.email;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.mailgun.util.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import swyp.swyp6_team7.global.email.template.EmailMessage;

import java.util.Locale;

@Component
public class EmailSenderProcessor {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderProcessor.class);

    private final TemplateEngine templateEngine;
    private final MailgunMessagesApi mailgunMessagesApi;

    public EmailSenderProcessor(TemplateEngine templateEngine, MailgunMessagesApi mailgunMessagesApi) {
        this.templateEngine = templateEngine;
        this.mailgunMessagesApi = mailgunMessagesApi;
    }

    public void process(EmailMessage message) {
        // Thymeleaf Context 설정
        Context ctx = new Context(Locale.KOREAN, message.getContext());
        String template = message.getTemplateName().getIdentifier();
        String body = templateEngine.process(template, ctx).trim();

        // Mailgun Message 생성
        Message messageTemp = Message.builder()
                .from(EmailUtil.nameWithEmail(EmailMessage.FROM_NAME, EmailMessage.FROM_EMAIL))
                .to(message.getRecipients())
                .subject(EmailMessage.TITLE_PREFIX + " " + message.getTitle())
                .html(body)
                .build();

        try {
            // Mailgun API로 메시지 발송
            var response = mailgunMessagesApi.sendMessage("moing.io", messageTemp);
            log.info("Send email to {}. response: {}", message.getEmail(), response.getMessage());
        } catch (Exception e) {
            log.error("메일 발송에 실패했습니다. {}, error: {}", message.getEmail(), e.getMessage());
            throw e;
        }

        log.info("메일 발송에 성공했습니다. {}", message.getEmail());
    }
}
