package se.s373746.Lab4.service;

import se.s373746.Lab4.entity.RefreshTokenEntity;

import java.util.Optional;

public interface RefreshTokenService {

    Optional<String> createRefreshToken(Long userId);

    Optional<String> updateRefreshToken(Long userId);

    int deleteByUserId(Long userId);

    Optional<RefreshTokenEntity> findByRefreshTokenName(String refreshToken);
}
