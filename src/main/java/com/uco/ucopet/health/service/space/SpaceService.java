package com.uco.ucopet.health.service.space;

import com.uco.ucopet.health.domain.space.SpaceDomain;
import com.uco.ucopet.health.repository.space.ISpaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

//Reglas de negocio de Space en el procesador de Salud.
//La API valida lo mismo para dar una respuesta rapida, pero aqui es donde mandan:
//entre que la API publico el mensaje y este lo consume, la BD pudo cambiar.

@Service
public class SpaceService {

    private static final Logger log = LoggerFactory.getLogger(SpaceService.class);

    private final ISpaceRepository spaceRepository;

    public SpaceService(ISpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    // CREATE y UPDATE llegan por la misma cola: si el id no existe se crea; si existe se actualiza.
    @Transactional
    public void saveSpace(SpaceDomain space) {
        if (space == null || space.getId() == null) {
            throw new IllegalArgumentException("El mensaje no trae el id del espacio.");
        }

        Optional<SpaceDomain> existing = spaceRepository.findById(space.getId());

        if (existing.isPresent()) {
            updateSpace(existing.get(), space);
        } else {
            createSpace(space);
        }
    }

    @Transactional
    public void deactivateSpace(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El mensaje no trae el id del espacio.");
        }

        SpaceDomain space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un espacio con id: " + id));

        space.setActive(false);
        spaceRepository.save(space);
        log.info("[SPACE] desactivado id={}", id);
    }

    private void createSpace(SpaceDomain message) {
        String code = trimmed(message.getCode());
        String type = trimmed(message.getType());
        String description = trimmed(message.getDescription());

        if (code.isBlank() || type.isBlank() || description.isBlank()) {
            throw new IllegalArgumentException("Ningún campo de texto (código, tipo o descripción) puede estar vacío o en blanco.");
        }
        if (code.length() > 10) {
            throw new IllegalArgumentException("El código no puede superar los 10 caracteres.");
        }
        if (!code.matches("^[A-Z]{3}-\\d+$")) {
            throw new IllegalArgumentException("El código debe tener el formato de 3 letras mayúsculas, un guion y números (Ej: CON-101).");
        }
        validateType(type);
        validateDescription(description);

        if (spaceRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("El código del espacio ya existe en el sistema.");
        }

        Boolean active = Optional.ofNullable(message.getActive()).orElse(true);

        spaceRepository.save(new SpaceDomain(message.getId(), code, type, description, active));
        log.info("[SPACE] creado id={} code={}", message.getId(), code);
    }

    // Solo se aplican los campos que vienen (los null significan "sin cambio").
    // El code no se modifica: es el identificador de negocio.
    private void updateSpace(SpaceDomain space, SpaceDomain changes) {
        if (changes.getType() != null) {
            space.setType(validateType(changes.getType().trim()));
        }
        if (changes.getDescription() != null) {
            space.setDescription(validateDescription(changes.getDescription().trim()));
        }
        if (changes.getActive() != null) {
            space.setActive(changes.getActive());
        }

        spaceRepository.save(space);
        log.info("[SPACE] actualizado id={}", space.getId());
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private String validateType(String type) {
        if (type.isBlank() || type.length() < 5 || type.length() > 50) {
            throw new IllegalArgumentException("El tipo de espacio debe tener entre 5 y 50 caracteres.");
        }
        return type;
    }

    private String validateDescription(String description) {
        if (description.isBlank() || description.length() < 10 || description.length() > 255) {
            throw new IllegalArgumentException("La descripción debe tener entre 10 y 255 caracteres.");
        }
        return description;
    }
}

