package com.dumbqr.dumbqr.service;

import com.dumbqr.dumbqr.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String supportEmail;

    @Async
    public void sendEmail(String to, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(supportEmail, "DumbQR");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        javaMailSender.send(message);
    }

    @Async
    public String sendVerificationEmail(User user){
        String subject = "Confirm your email address";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width\">\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "    <title>Email Verification</title>\n" +
                "    <style>\n" +
                "@media only screen and (max-width: 620px) {\n" +
                "  table[class=body] h1 {\n" +
                "    font-size: 24px !important;\n" +
                "    margin-bottom: 10px !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] p,\n" +
                "table[class=body] ul,\n" +
                "table[class=body] ol,\n" +
                "table[class=body] td,\n" +
                "table[class=body] span,\n" +
                "table[class=body] a {\n" +
                "    font-size: 16px !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .wrapper,\n" +
                "table[class=body] .article {\n" +
                "    padding: 10px !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .content {\n" +
                "    padding: 0 !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .container {\n" +
                "    padding: 0 !important;\n" +
                "    width: 100% !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .main {\n" +
                "    border-left-width: 0 !important;\n" +
                "    border-radius: 0 !important;\n" +
                "    border-right-width: 0 !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .btn table {\n" +
                "    width: 100% !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .btn a {\n" +
                "    width: 100% !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .img-responsive {\n" +
                "    height: auto !important;\n" +
                "    max-width: 100% !important;\n" +
                "    width: auto !important;\n" +
                "  }\n" +
                "}\n" +
                "@media all {\n" +
                "  .ExternalClass {\n" +
                "    width: 100%;\n" +
                "  }\n" +
                "\n" +
                "  .ExternalClass,\n" +
                ".ExternalClass p,\n" +
                ".ExternalClass span,\n" +
                ".ExternalClass font,\n" +
                ".ExternalClass td,\n" +
                ".ExternalClass div {\n" +
                "    line-height: 100%;\n" +
                "  }\n" +
                "\n" +
                "  .apple-link a {\n" +
                "    color: inherit !important;\n" +
                "    font-family: inherit !important;\n" +
                "    font-size: inherit !important;\n" +
                "    font-weight: inherit !important;\n" +
                "    line-height: inherit !important;\n" +
                "    text-decoration: none !important;\n" +
                "  }\n" +
                "\n" +
                "  .verification-code {\n" +
                "    font-size: 24px !important;\n" +
                "    font-weight: bold !important;\n" +
                "    letter-spacing: 2px !important;\n" +
                "    padding: 10px 25px !important;\n" +
                "    border: 1px solid #000 !important;\n" +
                "    border-radius: 10px !important;\n" +
                "    display: inline-block !important;\n" +
                "  }\n" +
                "}\n" +
                "</style>\n" +
                "  </head>\n" +
                "  <body class style=\"background-color: #ffffff; font-family: sans-serif; -webkit-font-smoothing: antialiased; font-size: 14px; line-height: 1.4; margin: 0; padding: 0; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;\">\n" +
                "    <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"body\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; background-color: #ffffff; width: 100%;\" width=\"100%\" bgcolor=\"#ffffff\">\n" +
                "      <tr>\n" +
                "        <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top;\" valign=\"top\">&nbsp;</td>\n" +
                "        <td class=\"container\" style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; display: block; max-width: 580px; padding: 10px; width: 580px; margin: 0 auto;\" width=\"580\" valign=\"top\">\n" +
                "          <div class=\"content\" style=\"box-sizing: border-box; display: block; margin: 0 auto; max-width: 580px; padding: 10px;\">\n" +
                "\n" +
                "            <!-- START CENTERED GREEN CONTAINER -->\n" +
                "            <table role=\"presentation\" class=\"main\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; background: #c2f5a0; border-radius: 20px; width: 100%;\" width=\"100%\">\n" +
                "\n" +
                "              <!-- START MAIN CONTENT AREA -->\n" +
                "              <tr>\n" +
                "                <td class=\"wrapper\" style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; box-sizing: border-box; padding: 20px;\" valign=\"top\">\n" +
                "                  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; width: 100%;\" width=\"100%\">\n" +
                "                    <tr>\n" +
                "                      <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; text-align: center;\" valign=\"top\" align=\"center\">\n" +
                "                          <h1 style=\"color: #000000 !important; font-family: sans-serif; font-weight: bold; margin: 0; margin-bottom: 20px; font-size: 24px; text-align: center;\">DumbQR.xyz</h1>\n" +
                "                      </td>\n" +
                "                    </tr>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "\n" +
                "              <!-- WHITE CONTAINER FOR VERIFICATION -->\n" +
                "              <tr>\n" +
                "                <td style=\"padding: 0 20px 20px 20px;\">\n" +
                "                  <table role=\"presentation\" class=\"inner-container\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; background: #ffffff; border-radius: 15px; width: 100%;\" width=\"100%\">\n" +
                "                    <tr>\n" +
                "                      <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; padding: 20px; text-align: center;\" valign=\"top\" align=\"center\">\n" +
                "                        <h2 style=\"color: #000000; font-family: sans-serif; font-weight: bold; margin: 0; margin-bottom: 20px; font-size: 20px;\">Confirm your email</h2>\n" +
                "                        <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px;\">You're almost there! We just need to confirm your email address<br>to create your account.</p>\n" +
                "                        \n" +
                "                        <div style=\"margin: 25px 0;\">\n" +
                "                          <span class=\"verification-code\" style=\"font-family: monospace;\">"+verificationCode+"</span>\n" +
                "                        </div>\n" +
                "                        \n" +
                "                        <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px; color: #555555;\">The verification code is valid for 5 minutes</p>\n" +
                "                      </td>\n" +
                "                    </tr>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "\n" +
                "            <!-- FOOTER -->\n" +
                "            <div class=\"footer\" style=\"clear: both; margin-top: 10px; text-align: center; width: 100%;\">\n" +
                "              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; width: 100%;\" width=\"100%\">\n" +
                "                <tr>\n" +
                "                  <td class=\"content-block\" style=\"font-family: sans-serif; vertical-align: top; padding-bottom: 10px; padding-top: 10px; color: #555555; font-size: 12px; text-align: center;\" valign=\"top\" align=\"center\">\n" +
                "                    <span class=\"apple-link\" style=\"color: #555555; font-size: 12px; text-align: center;\">"+supportEmail+"</span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </div>\n" +
                "\n" +
                "          </div>\n" +
                "        </td>\n" +
                "        <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top;\" valign=\"top\">&nbsp;</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";

        try{
            sendEmail(user.getEmail(), subject, htmlMessage);
            return "Success";
        } catch (Exception e){
            return "Error sending mail "+e.getMessage();
        }
    }

    @Async
    public String sendForgotPasswordEmail(User user){
        String subject = "DumbQR Password reset";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width\">\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "    <title>Email Verification</title>\n" +
                "    <style>\n" +
                "@media only screen and (max-width: 620px) {\n" +
                "  table[class=body] h1 {\n" +
                "    font-size: 24px !important;\n" +
                "    margin-bottom: 10px !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] p,\n" +
                "table[class=body] ul,\n" +
                "table[class=body] ol,\n" +
                "table[class=body] td,\n" +
                "table[class=body] span,\n" +
                "table[class=body] a {\n" +
                "    font-size: 16px !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .wrapper,\n" +
                "table[class=body] .article {\n" +
                "    padding: 10px !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .content {\n" +
                "    padding: 0 !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .container {\n" +
                "    padding: 0 !important;\n" +
                "    width: 100% !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .main {\n" +
                "    border-left-width: 0 !important;\n" +
                "    border-radius: 0 !important;\n" +
                "    border-right-width: 0 !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .btn table {\n" +
                "    width: 100% !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .btn a {\n" +
                "    width: 100% !important;\n" +
                "  }\n" +
                "\n" +
                "  table[class=body] .img-responsive {\n" +
                "    height: auto !important;\n" +
                "    max-width: 100% !important;\n" +
                "    width: auto !important;\n" +
                "  }\n" +
                "}\n" +
                "@media all {\n" +
                "  .ExternalClass {\n" +
                "    width: 100%;\n" +
                "  }\n" +
                "\n" +
                "  .ExternalClass,\n" +
                ".ExternalClass p,\n" +
                ".ExternalClass span,\n" +
                ".ExternalClass font,\n" +
                ".ExternalClass td,\n" +
                ".ExternalClass div {\n" +
                "    line-height: 100%;\n" +
                "  }\n" +
                "\n" +
                "  .apple-link a {\n" +
                "    color: inherit !important;\n" +
                "    font-family: inherit !important;\n" +
                "    font-size: inherit !important;\n" +
                "    font-weight: inherit !important;\n" +
                "    line-height: inherit !important;\n" +
                "    text-decoration: none !important;\n" +
                "  }\n" +
                "\n" +
                "  .verification-code {\n" +
                "    font-size: 24px !important;\n" +
                "    font-weight: bold !important;\n" +
                "    letter-spacing: 2px !important;\n" +
                "    padding: 10px 25px !important;\n" +
                "    border: 1px solid #000 !important;\n" +
                "    border-radius: 10px !important;\n" +
                "    display: inline-block !important;\n" +
                "  }\n" +
                "}\n" +
                "</style>\n" +
                "  </head>\n" +
                "  <body class style=\"background-color: #ffffff; font-family: sans-serif; -webkit-font-smoothing: antialiased; font-size: 14px; line-height: 1.4; margin: 0; padding: 0; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;\">\n" +
                "    <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"body\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; background-color: #ffffff; width: 100%;\" width=\"100%\" bgcolor=\"#ffffff\">\n" +
                "      <tr>\n" +
                "        <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top;\" valign=\"top\">&nbsp;</td>\n" +
                "        <td class=\"container\" style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; display: block; max-width: 580px; padding: 10px; width: 580px; margin: 0 auto;\" width=\"580\" valign=\"top\">\n" +
                "          <div class=\"content\" style=\"box-sizing: border-box; display: block; margin: 0 auto; max-width: 580px; padding: 10px;\">\n" +
                "\n" +
                "            <!-- START CENTERED GREEN CONTAINER -->\n" +
                "            <table role=\"presentation\" class=\"main\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; background: #c2f5a0; border-radius: 20px; width: 100%;\" width=\"100%\">\n" +
                "\n" +
                "              <!-- START MAIN CONTENT AREA -->\n" +
                "              <tr>\n" +
                "                <td class=\"wrapper\" style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; box-sizing: border-box; padding: 20px;\" valign=\"top\">\n" +
                "                  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; width: 100%;\" width=\"100%\">\n" +
                "                    <tr>\n" +
                "                      <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; text-align: center;\" valign=\"top\" align=\"center\">\n" +
                "                           <h1 style=\"color: #000000 !important; font-family: sans-serif; font-weight: bold; margin: 0; margin-bottom: 20px; font-size: 24px; text-align: center;\">DumbQR.xyz</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "\n" +
                "              <!-- WHITE CONTAINER FOR VERIFICATION -->\n" +
                "              <tr>\n" +
                "                <td style=\"padding: 0 20px 20px 20px;\">\n" +
                "                  <table role=\"presentation\" class=\"inner-container\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; background: #ffffff; border-radius: 15px; width: 100%;\" width=\"100%\">\n" +
                "                    <tr>\n" +
                "                      <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top; padding: 20px; text-align: center;\" valign=\"top\" align=\"center\">\n" +
                "                        <h2 style=\"color: #000000; font-family: sans-serif; font-weight: bold; margin: 0; margin-bottom: 20px; font-size: 20px;\">Forgot Password</h2>\n" +
                "                        <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px;\">Enter the below verification code to reset your password</p>\n" +
                "                        \n" +
                "                        <div style=\"margin: 25px 0;\">\n" +
                "                          <span class=\"verification-code\" style=\"font-family: monospace;\">"+verificationCode+"</span>\n" +
                "                        </div>\n" +
                "                        \n" +
                "                        <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; margin-bottom: 15px; color: #555555;\">The verification code is valid for 5 minutes</p>\n" +
                "                      </td>\n" +
                "                    </tr>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "\n" +
                "            <!-- FOOTER -->\n" +
                "            <div class=\"footer\" style=\"clear: both; margin-top: 10px; text-align: center; width: 100%;\">\n" +
                "              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; width: 100%;\" width=\"100%\">\n" +
                "                <tr>\n" +
                "                  <td class=\"content-block\" style=\"font-family: sans-serif; vertical-align: top; padding-bottom: 10px; padding-top: 10px; color: #555555; font-size: 12px; text-align: center;\" valign=\"top\" align=\"center\">\n" +
                "                    <span class=\"apple-link\" style=\"color: #555555; font-size: 12px; text-align: center;\">"+supportEmail+"</span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </div>\n" +
                "\n" +
                "          </div>\n" +
                "        </td>\n" +
                "        <td style=\"font-family: sans-serif; font-size: 14px; vertical-align: top;\" valign=\"top\">&nbsp;</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";

        try{
            sendEmail(user.getEmail(), subject, htmlMessage);
            return "Success";
        } catch (Exception e){
            return "Error sending mail "+e.getMessage();
        }
    }
}
