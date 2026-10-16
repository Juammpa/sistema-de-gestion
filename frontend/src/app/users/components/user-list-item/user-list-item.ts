import { Component, computed, input } from '@angular/core';
import translateRole from '../../utils/user-role.utils';
import { User } from '../../interfaces/user.interface';

@Component({
  selector: 'user-list-item',
  imports: [],
  templateUrl: './user-list-item.html',
})
export class UserListItem {
  translateRole = translateRole;

  // Input
  user = input.required<User>();

  // Nombre completo; si no tiene, se muestra el usuario
  displayName = computed(() => {
    const { firstName, lastName, username } = this.user();
    return `${firstName ?? ''} ${lastName ?? ''}`.trim() || username;
  });

  // Letras del avatar: iniciales de las dos primeras palabras del nombre
  initials = computed(
    () =>
      this.displayName()
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((word) => word[0].toUpperCase())
        .join('') || '?',
  );

  // Colores del badge segun el rol
  roleBadgeClass = computed(() => {
    switch (this.user().role) {
      case 'ADMIN':
        return 'border-yellow-500/30 bg-yellow-500/10 text-yellow-300';

      case 'USER':
        return 'border-sky-600/50 bg-sky-700/30 text-sky-300';

      default:
        return 'border-rose-500/30 bg-rose-500/10 text-rose-300';
    }
  });
}
