package aor.paj.mapper;

import aor.paj.dto.TokenDto;
import aor.paj.entity.TokenEntity;

public class TokenMapper {
    public static TokenDto convertTokenEntityToTokenDto(TokenEntity tokenEntity) {
        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken(tokenEntity.getToken());
        tokenDto.setExpiration(tokenEntity.getExpiration());
        tokenDto.setUser(UserMapper.convertUserEntityToUserDto(tokenEntity.getUser()));
        return tokenDto;
    }

    public static TokenEntity convertTokenDtoToTokenEntity(TokenDto tokenDto) {
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(tokenDto.getToken());
        tokenEntity.setExpiration(tokenDto.getExpiration());
        tokenEntity.setUser(UserMapper.convertUserDtoToUserEntity(tokenDto.getUser()));
        return tokenEntity;
    }
}
