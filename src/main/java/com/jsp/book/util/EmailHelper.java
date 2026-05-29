package com.jsp.book.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.jsp.book.entity.BookedTicket;
import com.jsp.book.entity.User;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailHelper {

    // ================= CONSTANTS =================

    private static final String FROM_NAME = "Book-My-Ticket";

    private static final String SUBJECT =
            "OTP for Creating Account with BookMyTicket";

    private static final String TEMPLATE =
            "email-template.html";

    // ================= CONFIGURED EMAIL =================

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ================= DEPENDENCIES =================

    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;

    // ================= CONSTRUCTOR =================

    public EmailHelper(
            JavaMailSender mailSender,
            TemplateEngine templateEngine) {

        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    // ================= SEND OTP =================

    @Async
    public void sendOtp(int otp, String name, String email) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail, FROM_NAME);

            helper.setTo(email);

            helper.setSubject(SUBJECT);

            Context context = new Context();

            context.setVariable("name", name);
            context.setVariable("otp", otp);

            String body =
                    templateEngine.process(TEMPLATE, context);

            helper.setText(body, true);

            mailSender.send(message);

            System.out.println("OTP mail sent successfully");

        } catch (Exception ex) {

            System.err.println(
                    "Failed to send OTP mail: "
                            + ex.getMessage());
        }
    }

    // ================= SEND TICKET DETAILS =================

    @Async
    public void sendTicketDetails(
            BookedTicket ticket,
            User user) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail, FROM_NAME);

            helper.setTo(user.getEmail());

            helper.setSubject(
                    "Ticket Booking Confirmation - "
                            + ticket.getMovieName());

            String body =
                    "<h2>Ticket Confirmed!</h2>"
                    + "<p>Dear "
                    + user.getName()
                    + ",</p>"

                    + "<p>Your booking for <b>"
                    + ticket.getMovieName()
                    + "</b> has been confirmed.</p>"

                    + "<p><b>Theater:</b> "
                    + ticket.getTheaterName()
                    + " ("
                    + ticket.getScreenName()
                    + ")</p>"

                    + "<p><b>Date & Time:</b> "
                    + ticket.getShowDate()
                    + " at "
                    + ticket.getShowTiming()
                    + "</p>"

                    + "<p><b>Seats:</b> "
                    + String.join(", ",
                            ticket.getSeatNumber())
                    + "</p>"

                    + "<p><b>Amount Paid:</b> Rs. "
                    + ticket.getTicketPrice()
                    + "</p>"

                    + "<p><b>Payment Method:</b> "
                    + ("CASH".equalsIgnoreCase(ticket.getPaymentId()) ? "Cash at venue" : "Online payment")
                    + "</p>"

                    + "<p>Thank you for choosing BookMyTicket!</p>";

            helper.setText(body, true);

            mailSender.send(message);

            System.out.println(
                    "Ticket confirmation mail sent successfully");

            // SMS Simulation

            System.out.println(
                    "\n================ SMS NOTIFICATION ================");

            System.out.println(
                    "To: " + user.getMobile());

            System.out.println(
                    "Message: Confirmed! "
                            + ticket.getMovieName()
                            + " at "
                            + ticket.getTheaterName()
                            + " on "
                            + ticket.getShowDate()
                            + " "
                            + ticket.getShowTiming()
                            + ". Seats: "
                            + String.join(", ",
                                    ticket.getSeatNumber()));

            System.out.println(
                    "=================================================\n");

        } catch (Exception ex) {

            System.err.println(
                    "Failed to send ticket details mail: "
                            + ex.getMessage());
        }
    }
}