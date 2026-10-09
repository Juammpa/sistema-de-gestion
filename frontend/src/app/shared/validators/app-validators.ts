import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

// Misma regex que el backend (ClientRequestDto.phoneNumber)
const PHONE_NUMBER_REGEX = /^\+?[0-9. ()-]{7,25}$/;

// Empieza con letra; luego letras, espacios, apostrofe, punto y guion.
const PERSON_NAME_REGEX = /^\p{L}[\p{L} '.-]*$/u;

export class AppValidators {
  // Rechaza textos con solo espacios
  static noBlank(control: AbstractControl): ValidationErrors | null {
    const value = control.value;

    return typeof value === 'string' && value.length > 0 && value.trim() === ''
      ? { blank: true }
      : null;
  }

  // Largo minimo/maximo contando el texto SIN espacios en los extremos
  static trimmedLength(min: number, max: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (typeof value !== 'string') return null;

      const length = value.trim().length;

      if (length === 0) return null; // lo resuelve "required" / "noBlank"
      if (length < min)
        return {
          minLength: { requiredLength: min, actualLength: length },
        };
      if (length > max)
        return {
          maxLength: { requiredLength: max, actualLength: length },
        };

      return null;
    };
  }

  static personName(control: AbstractControl): ValidationErrors | null {
    return AppValidators.matches(control, PERSON_NAME_REGEX, 'personName');
  }

  static phoneNumber(control: AbstractControl): ValidationErrors | null {
    return AppValidators.matches(control, PHONE_NUMBER_REGEX, 'phoneNumber');
  }

  // Valida contra una regex ignorando espacios en los extremos y valores vacios
  private static matches(
    control: AbstractControl,
    regex: RegExp,
    errorKey: string,
  ): ValidationErrors | null {
    const value = control.value;
    if (typeof value !== 'string' || value.trim() === '') return null;

    return regex.test(value.trim()) ? null : { [errorKey]: true };
  }
}
