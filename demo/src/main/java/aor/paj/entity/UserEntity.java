package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;


@Entity
@Table(name="user")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.token_verification = :token")
@NamedQuery(name = "User.findUserById", query = "SELECT u FROM UserEntity u WHERE u.id = :id")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u WHERE u.id != 1 AND u.id != 2")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name="token_verification", nullable = true, unique = true, updatable = true)
    private String token_verification;

    @Column(name="token_expiration", nullable = true, unique = false, updatable = true)
    private Instant token_expiration;

    @Column(name="active", nullable = false, unique = false, updatable = true)
    private Boolean active;

    @Column(name="created", nullable = false, unique = false, updatable = true)
    private Instant created;

    @Column(name="confirmed", nullable = false, unique = false, updatable = true)
    private Boolean confirmed = false;



    public UserEntity() {
    }

    public int getId() {
        return id;
    }

//    public void setId(int id) {
//        this.id = id;
//    }

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

    public String getToken_verification() {
        return token_verification;
    }

    public void setToken_verification(String token_verification) {
        this.token_verification = token_verification;
    }

    public Instant getToken_expiration() {
        return token_expiration;
    }

    public void setToken_expiration(Instant token_expiration) {
        this.token_expiration = token_expiration;
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

    public Instant getCreated() {
        return created;
    }
    //@PrePersist // This method is called just before the entity is persisted to the database
    public void setCreated(Instant created) {
        this.created = created;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
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
                ", token_verification='" + token_verification + '\'' +
                ", token_expiration=" + token_expiration +
                ", active=" + active +
                ", created=" + created +
                ", confirmed=" + confirmed +
                '}';
    }
}
