package in.vyomsoft.todo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        // This should match your spring.mail.username
        message.setFrom("your-dedicated-app-email@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Noti: Password Reset OTP");

        String emailBody = "Hello,\n\n" +
                "You requested a password reset for your Noti account.\n" +
                "Your OTP code is: " + otp + "\n\n" +
                "This code is valid for 5 minutes. If you did not request this, please ignore this email.";

        message.setText(emailBody);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error (crucial for debugging SMTP issues)
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}