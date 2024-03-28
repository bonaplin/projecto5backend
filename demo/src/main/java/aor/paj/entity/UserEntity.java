package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(name="user")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.token = :token")
@NamedQuery(name = "User.findUserById", query = "SELECT u FROM UserEntity u WHERE u.id = :id")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u WHERE u.id != 1 AND u.id != 2")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name="username", nullable = false, unique = true, updatable = false)
    private String username;

    @Column(name="password", nullable = false, unique = false, updatable = true)
    private String password;

    @Column(name="email", nullable = false, unique = true, updatable = true)
    private String email;

    @Column(name="firstname", nullable = false, unique = false, updatable = true)
    private String firstname;

    @Column(name="lastname", nullable = false, unique = false, updatable = true)
    private String lastname;

    @Column(name="phone", nullable = false, unique = false, updatable = true)
    private String phone;

    @Column(name="photoURL", nullable = false, unique = false, updatable = true)
    private String photoURL;

    @Column(name="role", nullable = false, unique = false, updatable = true)
    private String role;

    @Column(name="token", nullable = true, unique = true, updatable = true)
    private String token;

    @Column(name="active", nullable = false, unique = false, updatable = true)
    private Boolean active;

    public UserEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;//BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }


    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", phone='" + phone + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", role='" + role + '\'' +
                ", token='" + token + '\'' +
                ", active=" + active +
                '}';
    }
}
