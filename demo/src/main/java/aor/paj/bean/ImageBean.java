package aor.paj.bean;

import aor.paj.dao.UserDao;
import aor.paj.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@ApplicationScoped
public class ImageBean {
    @Inject
    UserDao userDao;


    private static final String IMAGE_DIRECTORY = "projeto-5/images";

    public String saveImage(InputStream imageData, String originalFileName, int id) throws IOException {
        String fileExtension = getFileExtension(originalFileName);
        String fileName = "profile." + fileExtension;
        String directory = IMAGE_DIRECTORY + "/" + id;
        Path imagePath = Paths.get(directory, fileName);

        if (!Files.exists(imagePath.getParent())) {
            Files.createDirectories(imagePath.getParent());
        }
        Files.copy(imageData, imagePath, StandardCopyOption.REPLACE_EXISTING);

        return imagePath.toString();
    }

    public byte[] getImage(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        return Files.readAllBytes(path);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public void saveUserProfileImage(int userId, InputStream imageData, String originalFileName) throws IOException {
        UserEntity userEntity = userDao.findUserById(userId);
        if (userEntity == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        String imageType = "image/" + getFileExtension(originalFileName);
        String imagePath = saveImage(imageData, originalFileName, userId);

        userEntity.setProfileImagePath(imagePath);
        userEntity.setProfileImageType(imageType);
        userDao.merge(userEntity);
    }



}