import { Component, inject, output, signal } from '@angular/core';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../shared/utils/form-utils';
import { ROLE_OPTIONS } from '../../utils/user-role.utils';
import { AppValidators } from '../../../shared/validators/app-validators';
import { UserCreateRequest, UserRole } from '../../interfaces/user.interface';

// Sin caracteres que se confunden al leerlos o dictarlos (0/0, 1/l/I)
const PASSWORD_CHARS = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789';
const GENERATED_PASSWORD_LENGTH = 12;

@Component({
  selector: 'user-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './user-form-modal.html',
})
export class UserFormModal {
  private userService = inject(UserService);
  private notifications = inject(NotificationService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Outputs
  created = output();
  cancel = output();

  roleOptions = ROLE_OPTIONS;

  // Evita enviar dos veces el mismo alta
  isSaving = signal<boolean>(false);

  userForm: FormGroup = this.fb.group({
    username: [
      '',
      [
        Validators.required,
        AppValidators.noBlank,
        AppValidators.trimmedLength(3, 50),
        AppValidators.username,
      ],
    ],
    email: [
      '',
      [
        Validators.required,
        AppValidators.noBlank,
        AppValidators.trimmedLength(1, 100),
        Validators.email,
      ],
    ],
    firstName: [
      '',
      [
        Validators.required,
        AppValidators.noBlank,
        AppValidators.trimmedLength(1, 50),
        AppValidators.personName,
      ],
    ],
    lastName: [
      '',
      [
        Validators.required,
        AppValidators.noBlank,
        AppValidators.trimmedLength(1, 50),
        AppValidators.personName,
      ],
    ],
    role: ['USER' as UserRole, [Validators.required]],
    temporaryPassword: [
      '',
      [Validators.required, AppValidators.noBlank, AppValidators.trimmedLength(8, 128)],
    ],
  });

  close() {
    this.cancel.emit();
  }

  // Rellena la contraseña con una aleatoria
  generatePassword() {
    const values = crypto.getRandomValues(new Uint32Array(GENERATED_PASSWORD_LENGTH));
    const password = Array.from(
      values,
      (value) => PASSWORD_CHARS[value % PASSWORD_CHARS.length],
    ).join('');

    const control = this.userForm.controls['temporaryPassword'];
    control.setValue(password);
    control.markAsTouched();
  }

  onSave() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    if (this.isSaving()) return;

    // Textos sin espacios sobrantes
    const request: UserCreateRequest = this.formUtils.trimValues(this.userForm.value);

    this.isSaving.set(true);

    this.userService.createUser(request).subscribe({
      next: () => {
        this.notifications.success('Usuario creado');
        this.created.emit();
        this.close();
      },
      error: () => this.isSaving.set(false), // El aviso lo muestra el interceptor
    });
  }
}
