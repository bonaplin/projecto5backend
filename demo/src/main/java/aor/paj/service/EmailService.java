package aor.paj.service;

import aor.paj.bean.Log;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.controller.EmailRequest;
import aor.paj.controller.EmailSender;
import aor.paj.controller.EmailDto;
import aor.paj.dto.UserDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

@Path("/email")
public class EmailService {
    @Inject
    TokenBean tokenBean;
    @Inject
    Log log;

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
        log.logUserInfo(null,"Email sent successfully to "+emailRequest.getTo(), 1);
        return Response.ok("Email sent successfully").build();
    }

    @Path("/activate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendActivationEmail(EmailDto prop) {
        emailSender.sendVerificationEmail(prop.getTo(), prop.getUsername(), prop.getLink());
        log.logUserInfo(null, "Activation email sent successfully to "+prop.getTo(), 1);
        return Response.ok("Activation email sent successfully").build();
    }

    @Path("/password")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendPasswordResetEmail(EmailDto prop) {
        emailSender.sendPasswordResetEmail(prop.getTo(), prop.getUsername(), prop.getLink());
        log.logUserInfo(null, "Password reset email sent successfully to "+prop.getTo(), 1);
        return Response.ok("Password reset email sent successfully").build();
    }
}