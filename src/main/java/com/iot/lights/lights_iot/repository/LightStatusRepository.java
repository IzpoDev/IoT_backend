package com.iot.lights.lights_iot.repository;



import com.iot.lights.lights_iot.model.document.LightStatusDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository // Marca esta interfaz como un repositorio de Spring
public interface LightStatusRepository extends MongoRepository<LightStatusDocument, String> {

    // Método para encontrar registros de una cuadra específica, ordenados por timestamp descendente
    List<LightStatusDocument> findByCuadraOrderByTimestampDesc(String cuadra);

    // Método para encontrar el último estado registrado de una cuadra
    @Query("{ 'cuadra' : ?0 }") // Podrías usar una consulta si fuera más compleja
    LightStatusDocument findFirstByCuadraOrderByTimestampDesc(String cuadra);

    // Otros métodos útiles, si los necesitas
    List<LightStatusDocument> findByEstado(String estado);
    List<LightStatusDocument> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}