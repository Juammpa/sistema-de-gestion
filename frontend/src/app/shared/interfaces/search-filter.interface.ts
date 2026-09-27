export interface SelectOption {
  value: string;
  label: string;
}

// Un selector de filtro u ordenamiento que muestra search-input
export interface SearchFilter {
  key: string; // Identifica al filtro (ej. 'sort', 'specie')
  label: string; // Etiqueta accesible del <select>
  options: SelectOption[]; // La primera opcion es la seleccionada por defecto
}

// Lo que emite search-input cuando el usuario cambia un filtro
export interface FilterChange {
  key: string;
  value: string;
}
