import { UserRole } from '../interfaces/user.interface';

// Nombre del rol tal como se muestra en pantalla
export default function translateRole(role: UserRole | null): string {
  switch (role) {
    case 'ADMIN':
      return 'Administrador';

    case 'USER':
      return 'Usuario';

    default:
      return 'Sin rol';
  }
}

// Opciones para elegir el rol
export const ROLE_OPTIONS: { value: UserRole; label: string }[] = [
  { value: 'USER', label: 'Usuario' },
  { value: 'ADMIN', label: 'Administrador' },
];
