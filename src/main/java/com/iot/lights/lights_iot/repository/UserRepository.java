package com.iot.lights.lights_iot.repository;

import com.iot.lights.lights_iot.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.active = true")
    UserEntity findByUsername(@Param("username") String username);
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.active = true")
    UserEntity findByEmail(@Param("email") String email);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.active = :active")
    Optional<UserEntity> findByUsernameAndActive(@Param("username") String username,@Param("active") Boolean active);
}
