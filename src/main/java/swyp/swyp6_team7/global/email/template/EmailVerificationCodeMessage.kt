package swyp.swyp6_team7.global.email.template

data class EmailVerificationCodeMessage(
    override val email: String,
    val verificationCode: String,
    val mainText: String,
    val description: String,
) : EmailMessage {
    override val templateName: Template
        get() = Template.EMAIL_VERIFICATION_CODE
    override val title: String
        get() = "이메일 인증을 진행해주세요"
    override val recipients: List<String>
        get() = listOf(email)
    override val context: Map<String, Any>
        get() = mapOf(
            "email" to email,
            "verificationCode" to verificationCode,
            "mainText" to mainText,
            "description" to description
        )
}
