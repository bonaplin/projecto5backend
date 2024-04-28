package aor.paj.bean;

import aor.paj.utils.TokenStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

@Named
@ApplicationScoped
public class Log {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Log.class);
    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

    @Inject
    TokenBean tokenBean;

    @Context
    public void setHttpServletRequest(HttpServletRequest request) {
        requestHolder.set(request);
    }

    public void logUserInfo(String token, String action, int type) {
        if (token == null || token.isEmpty()) {
            logger.warn(action);
            return;
        }
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if(tokenStatus != TokenStatus.VALID){
            return;
        }
        String username = tokenBean.getUserByToken(token).getUsername();


        HttpServletRequest request = requestHolder.get();
        String ipAddressReq = request != null ? request.getRemoteAddr() : "localhost";

        ThreadContext.put("username", username);
        ThreadContext.put("ipAddress", ipAddressReq);

        switch (type){
            case 1:
                logger.info(action);
                break;
            case 2:
                logger.error(action);
                break;
            case 3:
                logger.warn(action);
                break;
            default:
                break;
        }
    }
}

enum LogType {
    INFO(1),
    ERROR(2),
    WARN(3);

    private final int value;

    LogType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
