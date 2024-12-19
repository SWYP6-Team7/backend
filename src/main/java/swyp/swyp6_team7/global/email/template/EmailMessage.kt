package swyp.swyp6_team7.global.email.template

interface EmailMessage {
    val email: String
    val templateName: Template
    val title: String
    val context: Map<String, Any>
    val recipients: List<String>

    companion object {
        const val FROM_EMAIL = "noreply@moing.shop"
        const val FROM_NAME = "모잉"
        val TITLE_PREFIX
            get() = "[모잉]"

        const val HTML_HEADER = """
            <!DOCTYPE html
            PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <title>Moing</title>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            </head>
            <table
                style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; width: 100% !important; height: 100% !important; margin: 0; padding: 0; padding: 20px 0 30px 0; background-color: #ffffff;"
                border="0" cellpadding="0" cellspacing="0" width="100%" id="bodyTable">
                <tr>
                    <td
                style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;">
                <table
                    style="border-collapse: collapse; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt; webkit-text-size-adjust: 100%;"
                    border="0" cellspacing="0" cellpadding="0" width="600">
                    <tr>
                        <td
                            style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;">
                            <img style="border: 0; outline: none; text-decoration: none; -ms-interpolation-mode: bicubic;"
                                src="https://www.moing.shop/images/homeLogo.png" width="124" alt="Moing" />
                        </td>
                    </tr>
                    <tr>
                        <td
                            style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;">
                            <div style="height:20px;"></div>
                        </td>
                    </tr>
        """

        const val HTML_FOOTER = """
            </table>
            </td>
            </tr>
            </table>
            </html>
        """
    }
}