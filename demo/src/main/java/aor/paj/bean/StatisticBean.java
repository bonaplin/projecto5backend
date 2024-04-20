package aor.paj.bean;

import aor.paj.dao.UserDao;
import aor.paj.dto.UserStatisticsDto;

import aor.paj.utils.MessageType;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.bean.HandleWebSockets;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class StatisticBean {
    @EJB
    private UserDao userDao;
    @EJB
    private HandleWebSockets handleWebSockets;
    @EJB
    private Notifier notifier;
    @EJB
    private StatisticBean statisticBean;


    public UserStatisticsDto getStatisticsUsers() {

        int unconfirmedUsers = userDao.getUnconfirmedUserCount();
        int confirmedUsers = userDao.getConfirmedUserCount();
        int countUsers = userDao.getUserCount();
        UserStatisticsDto userStatistics = new UserStatisticsDto(countUsers, confirmedUsers, unconfirmedUsers);
        return userStatistics;
    }

    public void sendUserStatistics(MessageType messageType) {
        UserStatisticsDto userStatisticsDto = statisticBean.getStatisticsUsers();
        String json = handleWebSockets.convertToJsonString(userStatisticsDto, MessageType.STATISTIC_USER);
        notifier.sendToAllProductOwnerSessions(json);
        System.out.println(json);
    }


}
