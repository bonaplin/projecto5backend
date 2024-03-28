package aor.paj.dto;

public class UserPartialDto {
    private String firstname;
    private String photourl;

    public UserPartialDto() {
    }

    public UserPartialDto(String firstname, String photourl) {
        this.firstname = firstname;
        this.photourl = photourl;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }
}
