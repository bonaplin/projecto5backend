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
import java.util.UUID;

@ApplicationScoped
public class ImageBean {
    @Inject
    UserDao userDao;


    private static final String IMAGE_DIRECTORY = "projeto-5/images";

    public String saveImage(InputStream imageData, String originalFileName) throws IOException {
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        Path imagePath = Paths.get(IMAGE_DIRECTORY, fileName);
        Files.copy(imageData, imagePath);
        return imagePath.toString();
    }

    public byte[] getImage(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        return Files.readAllBytes(path);
    }

    public void saveUserProfileImage(int userId, InputStream imageData, String originalFileName) throws IOException {
        UserEntity userEntity = userDao.findUserById(userId);
        if (userEntity == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        Path path = Paths.get(IMAGE_DIRECTORY);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        String imagePath = saveImage(imageData, originalFileName);

        userEntity.setProfileImagePath(imagePath);

        String type = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

        String imageType = "image/"+type;
        userEntity.setProfileImageType(imageType);

        System.out.println("Image path set to user entity");
        userDao.merge(userEntity);
    }

}