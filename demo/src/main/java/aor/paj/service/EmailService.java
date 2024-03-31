package aor.paj.service;

import aor.paj.controller.EmailRequest;
import aor.paj.controller.EmailSender;
import aor.paj.dto.EmailDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/email")
public class EmailService {

    private final EmailSender emailSender;

    public EmailService() {
        this.emailSender = new EmailSender();
    }

    @Path("/send")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendEmail(EmailRequest emailRequest) {
        emailSender.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getContent());
        return Response.ok("Email sent successfully").build();
    }

    @Path("/activate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendActivationEmail(EmailDto prop) {
        emailSender.sendVerificationEmail(prop.getTo(), prop.getUsername(), prop.getLink());
        return Response.ok("Activation email sent successfully").build();
    }

    @Path("/password")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendPasswordResetEmail(EmailDto prop) {
        emailSender.sendPasswordResetEmail(prop.getTo(), prop.getUsername(), prop.getLink());
        return Response.ok("Password reset email sent successfully").build();
    }
}