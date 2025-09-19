package org.hackcelestial.sportsbridge.Services;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class OTPService {

    @Autowired
    private JavaMailSender mailSender;

    private final int OTP_EXPIRY_MINUTES = 10;

    public void sendOtp(String email, HttpSession session) throws MessagingException {
        String otp = String.format("%06d", new Random().nextInt(999999));

        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", expiryTime);


        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Your SportsBridge OTP Code");

        String htmlContent = buildOtpEmailHtml(email, otp, OTP_EXPIRY_MINUTES);

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildOtpEmailHtml(String email, String otp, int expiryMinutes) {
        String template = """
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>SportsBridge OTP</title>
  <style>
    /* Basic mobile-friendly resets */
    body,table,td { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial; }
    img { border: 0; outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; }
    a { color: #1f7aec; text-decoration: none; }
    @media screen and (max-width:480px) {
      .container { width: 100% !important; padding: 16px !important; }
      .hero { font-size: 20px !important; }
      .otp { font-size: 28px !important; letter-spacing: 4px !important; }
    }
  </style>
</head>
<body style="margin:0; padding:0; background-color:#f4f6fb;">
  <table width="100%" cellpadding="0" cellspacing="0" role="presentation" style="background-color:#f4f6fb; padding: 24px 0;">
    <tr>
      <td align="center">
        <!-- container -->
        <table class="container" width="600" cellpadding="0" cellspacing="0" role="presentation" style="width:600px; background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 6px 20px rgba(22,28,37,0.08);">
          
          <!-- header -->
          <tr>
            <td style="padding:20px 24px; display:flex; align-items:center; gap:12px;">
              <!-- logo -->
              <img src="https://yourdomain.com/logo.png" alt="SportsBridge" width="48" height="48" style="display:block; border-radius:8px;">
              <div style="font-weight:700; font-size:18px; color:#0f1724;">SportsBridge</div>
            </td>
          </tr>


          <!-- hero -->
          <tr>
            <td style="padding:24px;">
              <h1 class="hero" style="margin:0 0 12px 0; font-size:22px; color:#0f1724; font-weight:700;">
                Hello %s,
              </h1>
              <p style="margin:0 0 20px 0; color:#475569; line-height:1.5;">
                Use the one-time password (OTP) below to sign in to your SportsBridge account. This code is <strong>valid for %d minutes</strong>.
              </p>


              <!-- OTP card -->
              <table width="100%" cellpadding="0" cellspacing="0" role="presentation" style="margin:20px 0; border-radius:8px; background:linear-gradient(180deg,#ffffff,#fbfdff); border:1px solid #eef2ff;">
                <tr>
                  <td style="padding:20px; text-align:center;">
                    <div class="otp" style="display:inline-block; font-size:30px; font-weight:700; letter-spacing:6px; background:#f1f7ff; padding:14px 22px; border-radius:8px; color:#0f1724;">
                      %s
                    </div>
                    <p style="margin:12px 0 0 0; color:#64748b; font-size:13px;">
                      If you didn't request this, please ignore this email or contact support.
                    </p>
                  </td>
                </tr>
              </table>

            </td>
          </tr>

          <!-- divider -->
          <tr>
            <td style="padding:0 24px;">
              <hr style="border:none; height:1px; background:#eef2ff; margin:0;">
            </td>
          </tr>

          <!-- footer -->
          <tr>
            <td style="padding:18px 24px 28px 24px; color:#64748b; font-size:13px;">
              <div style="display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap;">
                <div>
                  <div style="font-weight:700; color:#0f1724;">SportsBridge</div>
                  <div style="margin-top:6px;">Bringing athletes, coaches & sponsors together.</div>
                </div>
                <div style="text-align:right;">
                  <div style="margin-bottom:8px;">Need help? <a href="mailto:support@sportsbridge.com" style="color:#1f7aec;">support@sportsbridge.com</a></div>
                  <div style="font-size:12px; color:#9aa4b2;">© %d SportsBridge. All rights reserved.</div>
                </div>
              </div>
            </td>
          </tr>

        </table>
        <div style="max-width:600px; margin-top:12px; color:#94a3b8; font-size:12px;">
          If you did not request this OTP, please ignore this email.
        </div>
      </td>
    </tr>
  </table>
</body>
</html>    
        """;

        int year = LocalDateTime.now().getYear();

        return String.format(template, email, expiryMinutes, otp, year);
    }
}