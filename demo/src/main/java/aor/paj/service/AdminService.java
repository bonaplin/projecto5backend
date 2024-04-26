package aor.paj.service;

import aor.paj.bean.TokenBean;
import aor.paj.dto.TokenExpirationUpdateDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.TokenStatus;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin")
public class AdminService {

    @Inject
    TokenBean tokenBean;

    @PUT
    @Path("/token-expiration")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTokenExpiration(TokenExpirationUpdateDto tokenExpirationUpdateDto, @HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (!tokenBean.getUserByToken(token).getRole().equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
        }

        System.out.println("Updating token expiration time");
        tokenBean.changeTokenExpiration(tokenExpirationUpdateDto,token);
        //verificar se há valores no dto antes de atualizar
//        tokenBean.setDefaultTokenExpirationMinutes(tokenExpirationUpdateDto.getDefaultTokenExpirationMinutes(), token);
//        tokenBean.setPoTokenExpirationMinutes(tokenExpirationUpdateDto.getPoTokenExpirationMinutes(), token);

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Token expiration time updated"))).build();
    }

    @GET
    @Path("/token-expiration")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTokenExpiration(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (!tokenBean.getUserByToken(token).getRole().equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
        }

        TokenExpirationUpdateDto tokenExpirationUpdateDto = new TokenExpirationUpdateDto(tokenBean.getDefaultTokenExpirationMinutes(), tokenBean.getPoTokenExpirationMinutes());
        return Response.status(200).entity(JsonUtils.convertObjectToJson(tokenExpirationUpdateDto)).build();
    }

}
