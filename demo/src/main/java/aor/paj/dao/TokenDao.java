package aor.paj.dao;

import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
@Stateless
public class TokenDao extends AbstractDao<TokenEntity>{
    private static final long serialVersionUID = 1L;

    public TokenDao() {
        super(TokenEntity.class);
    }

    public TokenEntity findTokenByToken(String token) {
        try {
            return (TokenEntity) em.createNamedQuery("Token.findTokenByToken").setParameter("token", token)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public TokenEntity findTokenByUserId(int id) {
        try {
            return (TokenEntity) em.createNamedQuery("Token.findTokenByUserId").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByTokenString(String token) {
        TokenEntity tokenEntity = findTokenByToken(token);
        if (tokenEntity != null) {
            return tokenEntity.getUser();
        }
        return null;
    }

}
