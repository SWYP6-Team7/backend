package swyp.swyp6_team7.global.email.template;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmailVerificationCodeMessage implements EmailMessage {

    private final String email;
    private final String verificationCode;
    private final String mainText;
    private final String description;

    public EmailVerificationCodeMessage(String email, String verificationCode, String mainText, String description) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.mainText = mainText;
        this.description = description;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getMainText() {
        return mainText;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public Template getTemplateName() {
        return Template.EMAIL_VERIFICATION_CODE;
    }

    @Override
    public String getTitle() {
        return "이메일 인증을 진행해주세요";
    }

    @Override
    public List<String> getRecipients() {
        return Collections.singletonList(email);
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
            "email", email,
            "verificationCode", verificationCode,
            "mainText", mainText,
            "description", description
        );
    }

    @Override
    public String toString() {
        return "EmailVerificationCodeMessage{" +
               "email='" + email + '\'' +
               ", verificationCode='" + verificationCode + '\'' +
               ", mainText='" + mainText + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}
