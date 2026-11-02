package com.uco.ucopet.health.service.procedure;

import com.uco.ucopet.health.domain.procedure.ProcedureDomain;
import com.uco.ucopet.health.domain.procedure.ProcedureItemDomain;
import com.uco.ucopet.health.repository.procedure.IProcedureRepository;
import com.uco.ucopet.health.repository.space.ISpaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ProcedureService {

    private static final Logger log = LoggerFactory.getLogger(ProcedureService.class);

    private static final int MAX_CODE_LENGTH = 50;
    private static final int MAX_TYPE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_DURATION_MINUTES = 1440;
    private static final int MAX_CODE_SEQUENCE = 999999;
    private static final String NUMERIC_CODE_REGEX = "^\\d+$";

    private final IProcedureRepository procedureRepository;
    private final ISpaceRepository spaceRepository;

    public ProcedureService(final IProcedureRepository procedureRepository,
                            final ISpaceRepository spaceRepository) {
        this.procedureRepository = procedureRepository;
        this.spaceRepository = spaceRepository;
    }

    @Transactional
    public void saveProcedure(final ProcedureDomain message) {
        if (message == null || message.getId() == null) {
            throw new IllegalArgumentException("El mensaje no trae el id del procedimiento.");
        }
        final Optional<ProcedureDomain> existing = procedureRepository.findById(message.getId());
        if (existing.isPresent()) {
            updateProcedure(existing.get(), message);
        } else {
            createProcedure(message);
        }
    }

    @Transactional
    public void changeProcedureStatus(final UUID id, final Boolean active) {
        if (id == null) {
            throw new IllegalArgumentException("El mensaje no trae el id del procedimiento.");
        }
        if (active == null) {
            throw new IllegalArgumentException("El mensaje no indica el estado del procedimiento.");
        }
        final ProcedureDomain procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un procedimiento con id: " + id));
        procedure.setActive(active);
        procedureRepository.save(procedure);
        log.info("[PROCEDURE] estado actualizado id={} active={}", id, active);
    }

    private void createProcedure(final ProcedureDomain message) {
        final String type = requiredText(message.getType(), "El tipo es obligatorio",
                "El tipo no puede superar los 100 caracteres", MAX_TYPE_LENGTH);
        final String description = requiredText(message.getDescription(), "La descripción es obligatoria",
                "La descripción no puede superar los 500 caracteres", MAX_DESCRIPTION_LENGTH);
        validateDuration(message.getDurationMinutes());
        validateSpaceIsActive(message.getSpaceId());
        final List<ProcedureItemDomain> items = validateItems(message.getDefaultItems());
        final String code = resolveCode(message.getCode(), null);

        procedureRepository.save(new ProcedureDomain(message.getId(), code, type, description,
                message.getDurationMinutes(), message.getSpaceId(), items, true));
        log.info("[PROCEDURE] creado id={} code={}", message.getId(), code);
    }

    private void updateProcedure(final ProcedureDomain procedure, final ProcedureDomain changes) {
        if (changes.getType() != null) {
            procedure.setType(requiredText(changes.getType(), "El tipo es obligatorio",
                    "El tipo no puede superar los 100 caracteres", MAX_TYPE_LENGTH));
        }
        if (changes.getDescription() != null) {
            procedure.setDescription(requiredText(changes.getDescription(), "La descripción es obligatoria",
                    "La descripción no puede superar los 500 caracteres", MAX_DESCRIPTION_LENGTH));
        }
        if (changes.getDurationMinutes() != null) {
            validateDuration(changes.getDurationMinutes());
            procedure.setDurationMinutes(changes.getDurationMinutes());
        }
        if (changes.getSpaceId() != null) {
            validateSpaceIsActive(changes.getSpaceId());
            procedure.setSpaceId(changes.getSpaceId());
        }
        if (!changes.getDefaultItems().isEmpty()) {
            procedure.setDefaultItems(validateItems(changes.getDefaultItems()));
        }

        final String requestedCode = normalizeCode(changes.getCode());
        if (requestedCode != null) {
            procedure.setCode(resolveCode(requestedCode, procedure.getId()));
        } else if (procedure.getCode() == null) {
            procedure.setCode(generateNextCode());
        }

        procedureRepository.save(procedure);
        log.info("[PROCEDURE] actualizado id={}", procedure.getId());
    }

    private String resolveCode(final String requestedCode, final UUID currentId) {
        final String code = normalizeCode(requestedCode);
        if (code == null) {
            return generateNextCode();
        }
        validateCodeFormat(code);
        validateCodeIsAvailable(code, currentId);
        return code;
    }

    private void validateCodeFormat(final String code) {
        if (code.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("El código no puede superar los 50 caracteres");
        }
        if (!code.matches(NUMERIC_CODE_REGEX)) {
            throw new IllegalArgumentException("El código debe ser un número entero positivo");
        }
        final long numericCode;
        try {
            numericCode = Long.parseLong(code);
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("El código no tiene un formato válido");
        }
        if (numericCode <= 0) {
            throw new IllegalArgumentException("El código debe ser mayor que cero");
        }
        if (numericCode > MAX_CODE_SEQUENCE) {
            throw new IllegalArgumentException("El código no puede superar " + MAX_CODE_SEQUENCE);
        }
    }

    private void validateCodeIsAvailable(final String code, final UUID currentId) {
        procedureRepository.findByCode(code)
                .filter(procedure -> !procedure.getId().equals(currentId))
                .ifPresent(procedure -> {
                    throw new IllegalArgumentException("Ya existe un procedimiento con el código indicado");
                });
    }

    private String generateNextCode() {
        final int nextSequence = procedureRepository.findAllCodes().stream()
                .map(this::extractCodeSequence)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        if (nextSequence > MAX_CODE_SEQUENCE) {
            throw new IllegalArgumentException("No hay códigos disponibles para nuevos procedimientos");
        }
        return String.valueOf(nextSequence);
    }

    private int extractCodeSequence(final String code) {
        if (code == null || !code.matches(NUMERIC_CODE_REGEX)) {
            return 0;
        }
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException _) {
            return 0;
        }
    }

    private String requiredText(final String value, final String requiredMessage,
                                final String maxLengthMessage, final int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(requiredMessage);
        }
        final String cleaned = value.trim();
        if (cleaned.length() > maxLength) {
            throw new IllegalArgumentException(maxLengthMessage);
        }
        return cleaned;
    }

    private void validateDuration(final Integer durationMinutes) {
        if (durationMinutes == null) {
            throw new IllegalArgumentException("La duración en minutos es obligatoria");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("La duración en minutos debe ser mayor que cero");
        }
        if (durationMinutes > MAX_DURATION_MINUTES) {
            throw new IllegalArgumentException("La duración en minutos no puede superar 1440");
        }
    }

    private void validateSpaceIsActive(final UUID spaceId) {
        if (spaceId == null) {
            throw new IllegalArgumentException("El espacio es obligatorio");
        }
        final boolean active = spaceRepository.findById(spaceId)
                .map(space -> Boolean.TRUE.equals(space.getActive()))
                .orElse(false);
        if (!active) {
            throw new IllegalArgumentException("El espacio seleccionado no existe o no se encuentra activo");
        }
    }

    private List<ProcedureItemDomain> validateItems(final List<ProcedureItemDomain> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debes asociar al menos un producto o servicio predeterminado");
        }
        final Set<UUID> itemIds = new HashSet<>();
        final List<ProcedureItemDomain> validated = new ArrayList<>();
        for (ProcedureItemDomain item : items) {
            if (item == null) {
                throw new IllegalArgumentException(
                        "La información del producto o servicio predeterminado es obligatoria");
            }
            if (item.getItemId() == null) {
                throw new IllegalArgumentException(
                        "El identificador del producto o servicio predeterminado es obligatorio");
            }
            if (item.getQuantity() == null) {
                throw new IllegalArgumentException("La cantidad es obligatoria");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
            }
            if (!itemIds.add(item.getItemId())) {
                throw new IllegalArgumentException(
                        "Un producto o servicio predeterminado solo puede asociarse una vez");
            }
            validated.add(new ProcedureItemDomain(item.getItemId(), item.getQuantity()));
        }
        return validated;
    }

    private String normalizeCode(final String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        final String trimmedCode = code.trim();
        if (trimmedCode.matches(NUMERIC_CODE_REGEX)) {
            try {
                return String.valueOf(Long.parseLong(trimmedCode));
            } catch (NumberFormatException _) {
                return trimmedCode;
            }
        }
        return trimmedCode;
    }
}