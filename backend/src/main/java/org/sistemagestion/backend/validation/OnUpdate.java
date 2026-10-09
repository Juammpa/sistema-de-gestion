package org.sistemagestion.backend.validation;

import jakarta.validation.groups.Default;

// Grupo de validacion para las reglas que solo aplican al editar (ej. fecha obligatoria)
// Extiende Default: al validar con este grupo tambien se aplican las reglas comunes.
public interface OnUpdate extends Default {
}
