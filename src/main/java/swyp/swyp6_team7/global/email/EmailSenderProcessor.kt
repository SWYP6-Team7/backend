package swyp.swyp6_team7.global.email

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.model.message.Message
import com.mailgun.util.EmailUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import swyp.swyp6_team7.global.email.template.EmailMessage
import java.util.*

@Component
class EmailSenderProcessor(
    private val templateEngine: TemplateEngine,
    private val mailgunMessagesApi: MailgunMessagesApi
) {
    private val log = LoggerFactory.getLogger(EmailSenderProcessor::class.java)

    fun process(message: EmailMessage) {
        val ctx = Context(Locale.KOREAN, message.context)
        val template = message.templateName
        val body = templateEngine.process(template.identifier, ctx)

        val messageTemp = Message.builder()
            .from(EmailUtil.nameWithEmail(EmailMessage.FROM_NAME, EmailMessage.FROM_EMAIL))
            .to(message.recipients)
            .subject("${EmailMessage.TITLE_PREFIX} ${message.title}")
            .html((EmailMessage.HTML_HEADER + body + EmailMessage.HTML_FOOTER).trimIndent())

        val sendMessage = messageTemp.build()

        runCatching {
            val response = mailgunMessagesApi.sendMessage("moing.shop", sendMessage)
            log.info("Send email to ${message.email}. response: ${response.message}")
        }.onFailure {
            log.error("메일 발송에 실패했습니다. ${message.email}, error: ${it.message}")
            throw it
        }
    }
}