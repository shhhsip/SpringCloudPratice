package org.example.userservice.repository;

import org.example.userservice.dto.UserDto;
import org.example.userservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserId(String userId);
    UserEntity findByEmail(String username);
}
