// src/main/java/com/iot/lights/lights_iot/repository/LightStatusActualRepository.java
package com.iot.lights.lights_iot.repository;

import com.iot.lights.lights_iot.model.document.LightStatusActualDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LightStatusActualRepository extends MongoRepository<LightStatusActualDocument, String> {

}