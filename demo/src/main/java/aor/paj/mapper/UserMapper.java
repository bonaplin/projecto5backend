package aor.paj.mapper;

import aor.paj.dto.UserDto;
import aor.paj.entity.UserEntity;

public class UserMapper {
    public UserMapper() {
    }

    public static UserEntity convertUserDtoToUserEntity(UserDto userDto) {
        UserEntity userEntity = new UserEntity();

        userEntity.setEmail(userDto.getEmail());
        userEntity.setFirstname(userDto.getFirstname());
        userEntity.setLastname(userDto.getLastname());
        userEntity.setPassword(userDto.getPassword());
        userEntity.setUsername(userDto.getUsername());
        userEntity.setPhone(userDto.getPhone());
        userEntity.setPhotoURL(userDto.getPhotoURL());
        userEntity.setRole(userDto.getRole());
        userEntity.setActive(userDto.isActive());

        return userEntity;
    }

    public static UserDto convertUserEntityToUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();

        userDto.setEmail(userEntity.getEmail());
        userDto.setFirstname(userEntity.getFirstname());
        userDto.setLastname(userEntity.getLastname());
        userDto.setPassword(userEntity.getPassword());
        userDto.setUsername(userEntity.getUsername());
        userDto.setPhone(userEntity.getPhone());
        userDto.setPhotoURL(userEntity.getPhotoURL());
        userDto.setRole(userEntity.getRole());
        userDto.setActive(userEntity.isActive());

        return userDto;
    }
}
