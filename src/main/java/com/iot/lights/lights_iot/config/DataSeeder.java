package com.iot.lights.lights_iot.config;

import com.iot.lights.lights_iot.model.entity.RoleEntity;
import com.iot.lights.lights_iot.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Este código se ejecutará una vez al iniciar la aplicación
        crearRolSiNoExiste("ADMIN", "El Rol de Administrador tiene acceso global a la API");
        crearRolSiNoExiste("USER", "El Rol de Usuario tiene acceso limitado a la API");
    }

    private void crearRolSiNoExiste(String nombreRol, String descripcion) {
        // Verifica si el rol ya existe antes de crearlo
        if (roleRepository.findByName(nombreRol)==null) {
            RoleEntity nuevoRol = new RoleEntity();
            nuevoRol.setName(nombreRol);
            nuevoRol.setDescription(descripcion);
            roleRepository.save(nuevoRol);
            System.out.println("Rol por defecto creado: " + nombreRol);
        }
    }
}