package com.iot.lights.lights_iot.repository;



import com.iot.lights.lights_iot.model.document.LightStatusDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LightStatusRepository extends MongoRepository<LightStatusDocument, String> {

    List<LightStatusDocument> findByCuadraOrderByTimestampDesc(String cuadra);

    // ELIMINAR ESTE MÉTODO que causa el error:
    // LightStatusDocument findFirstByCuadraOrderByTimestampDesc(String cuadra);

    List<LightStatusDocument> findByEstado(String estado);
    List<LightStatusDocument> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // MANTENER SOLO ESTE:
    Optional<LightStatusDocument> findTop1ByCuadraOrderByTimestampDesc(String cuadra);
    List<LightStatusDocument> findByCuadra(String cuadra);
}