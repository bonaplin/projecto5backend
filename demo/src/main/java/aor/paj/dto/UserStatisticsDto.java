package aor.paj.dto;

import com.google.gson.Gson;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserStatisticsDto {
    private int countUsers;
    private int confirmedUsers;
    private int unconfirmedUsers;
    private int activeUsers;
    private int inactiveUsers;

    public UserStatisticsDto() {
    }

    public UserStatisticsDto(int countUsers, int confirmedUsers, int unconfirmedUsers, int activeUsers, int inactiveUsers) {
        this.countUsers = countUsers;
        this.confirmedUsers = confirmedUsers;
        this.unconfirmedUsers = unconfirmedUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
    }
    @XmlElement
    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    @XmlElement
    public int getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(int inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    @XmlElement
    public int getCountUsers() {
        return countUsers;
    }

    public void setCountUsers(int countUsers) {
        this.countUsers = countUsers;
    }

    @XmlElement
    public int getConfirmedUsers() {
        return confirmedUsers;
    }

    public void setConfirmedUsers(int confirmedUsers) {
        this.confirmedUsers = confirmedUsers;
    }

    @XmlElement
    public int getUnconfirmedUsers() {
        return unconfirmedUsers;
    }

    public void setUnconfirmedUsers(int unconfirmedUsers) {
        this.unconfirmedUsers = unconfirmedUsers;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }



}
