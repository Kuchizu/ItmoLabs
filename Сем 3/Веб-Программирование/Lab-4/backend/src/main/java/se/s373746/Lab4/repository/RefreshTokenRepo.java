package se.s373746.Lab4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.s373746.Lab4.entity.RefreshTokenEntity;
import se.s373746.Lab4.entity.UserEntity;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshTokenEntity, Long> {

    @Transactional
    int deleteByUserEntity(UserEntity userEntity);

    RefreshTokenEntity findByRefreshToken(String refreshToken);
}
