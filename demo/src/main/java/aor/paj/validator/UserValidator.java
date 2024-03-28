package aor.paj.validator;

import aor.paj.dao.UserDao;
import aor.paj.dto.UserDto;
import aor.paj.dto.UserPasswordUpdateDto;
import aor.paj.dto.UserUpdateDto;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@ApplicationScoped
public class UserValidator {

    // Regular expressions for email, phone number, and URL validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PHONE_REGEX = "^[+\\d\\s()-]*$";
    private static final String URL_REGEX = "^(http|https)://.*$";

    @EJB
    private static UserDao userDao;
    //function that authenticates the user
    public static boolean isValidUser(String token) {
        return userDao.findUserByToken(token) != null;
    }

    //function that authenticates the user by username
    public static boolean isValidUserByUsername(String username) {
        return userDao.findUserByUsername(username) != null;
    }

    //Check if the user already exists with the same username
    public static boolean userExists(UserDto user) {
        UserDto userDto = UserMapper.convertUserEntityToUserDto(userDao.findUserByUsername(user.getUsername()));

        if(userDto.getUsername() != null) {
            return true;
        }

        userDto = UserMapper.convertUserEntityToUserDto(userDao.findUserByEmail(user.getEmail()));

        if(userDto.getEmail() != null) {
            return true;
        }

        return false;
    }

    //Checks if the user and password match checing in database my sql
    public static boolean userPasswordMatch(String username, String password) {
        UserEntity user = userDao.findUserByUsername(username);
        if (user != null) {
            //if the decrypted password matches the password passed as a parameter
            if (BCrypt.checkpw(password, user.getPassword())) {
                return true;
            }
        }
        return false;
    }


    //Check if the user already exists with the same email
    public static boolean emailExists(List<UserDto> userDtos, String email) {
        for (UserDto u : userDtos) {
            if (u.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    // Helper method to validate email format using regex
    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Helper method to validate phone number format using regex
    public static boolean isValidPhoneNumber(String phone) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    // Helper method to validate URL format using regex
    public static boolean isValidURL(String url) {
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    //Function that verifys if user to add is null or blank
    public static boolean isNullorBlank(UserDto u) {
        return isNullOrBlank(u.getUsername()) || isNullOrBlank(u.getPassword()) || isNullOrBlank(u.getEmail())
                || isNullOrBlank(u.getFirstname()) || isNullOrBlank(u.getLastname())
                || isNullOrBlank(u.getPhone()) || isNullOrBlank(u.getPhotoURL());
    }

    //Function that verifys if user to update is null or blank
    public static boolean isNullorBlank(UserUpdateDto u) {
        return isNullOrBlank(u.getUsername()) || isNullOrBlank(u.getEmail())
                || isNullOrBlank(u.getFirstname()) || isNullOrBlank(u.getLastname())
                || isNullOrBlank(u.getPhone()) || isNullOrBlank(u.getPhotoURL());
    }

    //Function that verifys if user to update password is null or blank
    public static boolean isNullorBlank(UserPasswordUpdateDto u) {
        return isNullOrBlank(u.getOldPassword()) || isNullOrBlank(u.getNewPassword());
    }

    // Helper method to check if a string is null or blank
    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

}
