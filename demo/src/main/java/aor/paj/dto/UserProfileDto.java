package aor.paj.dto;

public class UserProfileDto {
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String photoURL;
    private int taskcount;
    private int todocount;
    private int doingcount;
    private int donecount;

    public UserProfileDto() {
    }

    public UserProfileDto(String firstname, String lastname, String username, String email, String photoURL, int todocount, int doingcount, int donecount) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.photoURL = photoURL;
        this.taskcount = taskcount;
        this.todocount = todocount;
        this.doingcount = doingcount;
        this.donecount = donecount;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getTaskcount() {
        return calculateTaskCount();
    }

    public int getTodocount() {
        return todocount;
    }

    public void setTodocount(int todocount) {
        this.todocount = todocount;
    }

    public int getDoingcount() {
        return doingcount;
    }

    public void setDoingcount(int doingcount) {
        this.doingcount = doingcount;
    }

    public int getDonecount() {
        return donecount;
    }

    public void setDonecount(int donecount) {
        this.donecount = donecount;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setTaskcount(int taskcount) {
        this.taskcount = taskcount;
    }

    private int calculateTaskCount() {
        return todocount + doingcount + donecount;
    }

}
