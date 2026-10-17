import { Component, computed, inject, input, linkedSignal, output, signal } from '@angular/core';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import { User, UserRole } from '../../interfaces/user.interface';
import { ROLE_OPTIONS } from '../../utils/user-role.utils';
import { getDisplayName } from '../../utils/user-display.utils';

@Component({
  selector: 'user-manage-modal',
  imports: [],
  templateUrl: './user-manage-modal.html',
})
export class UserManageModal {
  private userService = inject(UserService);
  private notifications = inject(NotificationService);
  private auth = inject(AuthService);

  // Input
  user = input.required<User>();

  // Outputs
  cancel = output();
  updated = output<User>(); // El usuario actualizado, para reemplazarlo en la lista.

  roleOptions = ROLE_OPTIONS;

  displayName = computed(() => getDisplayName(this.user()));

  // No se permite gestionar la propia cuenta
  isSelf = computed(() => this.user().id === this.auth.userId);

  // Rol elegido en el select. Un usuario sin rol arranca en "Usuario"
  selectedRole = linkedSignal<UserRole>(() => this.user().role ?? 'USER');
  roleChanged = computed(() => this.selectedRole() !== this.user().role);

  // Señales de estado
  isSavingRole = signal<boolean>(false);
  isTogglingAccess = signal<boolean>(false);
  confirmingDisable = signal<boolean>(false);

  close() {
    this.cancel.emit();
  }

  onRoleSelect(event: Event) {
    this.selectedRole.set((event.target as HTMLSelectElement).value as UserRole);
  }

  saveRole() {
    if (this.isSelf() || !this.roleChanged() || this.isSavingRole()) return;

    this.isSavingRole.set(true);

    this.userService.changeRol(this.user().id, this.selectedRole()).subscribe({
      next: (updatedUser) => {
        this.notifications.success('Rol actualizado');
        this.updated.emit(updatedUser);
        this.close();
      },
      error: () => this.isSavingRole.set(false), // El aviso lo muetra el interceptor
    });
  }

  disable() {
    if (this.isSelf() || this.isTogglingAccess()) return;

    this.isTogglingAccess.set(true);

    this.userService.disableUser(this.user().id).subscribe({
      next: () => {
        this.notifications.success('Usuario deshabilitado');
        this.updated.emit({ ...this.user(), enabled: false });
        this.close();
      },
      error: () => {
        this.isTogglingAccess.set(false);
        this.confirmingDisable.set(false);
      },
    });
  }

  enable() {
    if (this.isSelf() || this.isTogglingAccess()) return;

    this.isTogglingAccess.set(true);

    this.userService.activateUser(this.user().id).subscribe({
      next: () => {
        this.notifications.success('Usuario habilitado');
        this.updated.emit({ ...this.user(), enabled: true });
        this.close();
      },
      error: () => this.isTogglingAccess.set(false),
    });
  }
}
