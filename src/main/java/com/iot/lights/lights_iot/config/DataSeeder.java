package com.iot.lights.lights_iot.config;

import com.iot.lights.lights_iot.model.entity.RoleEntity;
import com.iot.lights.lights_iot.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j // Es mejor usar un logger que System.out.println
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        crearRolSiNoExiste("ADMIN", "El Rol de Administrador tiene acceso global a la API");
        crearRolSiNoExiste("USER", "El Rol de Usuario tiene acceso limitado a la API");
    }

    private void crearRolSiNoExiste(String nombreRol, String descripcion) {
        if (!roleRepository.existsByName(nombreRol)) {
            RoleEntity nuevoRol = new RoleEntity();
            nuevoRol.setName(nombreRol);
            nuevoRol.setDescription(descripcion);
            nuevoRol.setActive(true); // Es buena idea inicializar el estado activo.
            roleRepository.save(nuevoRol);
            log.info("Rol por defecto creado: {}", nombreRol);
        }
    }
}