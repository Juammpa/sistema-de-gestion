import { User } from '../interfaces/user.interface';

// Nombre completo; si no tiene se muestra el usuario
export function getDisplayName(user: User): string {
  return `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.username;
}
