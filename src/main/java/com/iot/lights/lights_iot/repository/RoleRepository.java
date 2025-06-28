package com.iot.lights.lights_iot.repository;

import com.iot.lights.lights_iot.model.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @Query("SELECT r FROM RoleEntity r WHERE r.name =: name")
    RoleEntity findByName(@Param("name") String name);

    @Query("SELECT r FROM RoleEntity r WHERE r.id =: id")
    Optional<RoleEntity> findById(@Param("id") Long id);
}
