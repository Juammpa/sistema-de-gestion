import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { toIsoDate } from '../utils/date.utils';

// Misma regex que el backend (ClientRequestDto.phoneNumber)
const PHONE_NUMBER_REGEX = /^\+?[0-9. ()-]{7,25}$/;

// Empieza con letra; luego letras, espacios, apostrofe, punto y guion.
const PERSON_NAME_REGEX = /^\p{L}[\p{L} '.-]*$/u;

// Letras, números, punto, guion y guion bajo
const USERNAME_REGEX = /^[A-Za-z0-9._-]+$/;

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
      if (length < min) return { minlength: { requiredLength: min, actualLength: length } };
      if (length > max) return { maxlength: { requiredLength: max, actualLength: length } };

      return null;
    };
  }

  static personName(control: AbstractControl): ValidationErrors | null {
    return AppValidators.matches(control, PERSON_NAME_REGEX, 'personName');
  }

  static phoneNumber(control: AbstractControl): ValidationErrors | null {
    return AppValidators.matches(control, PHONE_NUMBER_REGEX, 'phoneNumber');
  }

  static username(control: AbstractControl): ValidationErrors | null {
    return AppValidators.matches(control, USERNAME_REGEX, 'username');
  }

  // Importe: positivo, con coma o punto decimal
  static amount(max: number, maxDecimals = 2): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const raw = control.value;
      if (raw === null || raw === undefined || String(raw).trim() === '') return null;

      const text = String(raw).trim().replace(',', '.');
      if (!/^\d+(\.\d+)?$/.test(text)) return { amountFormat: true };

      const decimals = text.split('.')[1]?.length ?? 0;
      if (decimals > maxDecimals) return { decimals: { max: maxDecimals } };

      const value = Number(text);
      if (value <= 0) return { positive: true };
      if (value > max) return { max: { max, actual: value } };

      return null;
    };
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

  // Entero (sin decimales) entre min y max
  static integer(min: number, max: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const raw = control.value;
      if (raw === null || raw === undefined || String(raw).trim() === '') return null;

      const text = String(raw).trim();
      if (!/^\d+$/.test(text)) return { integer: true };

      const value = Number(text);
      if (value < min) return { min: { min, actual: value } };
      if (value > max) return { max: { max, actual: value } };

      return null;
    };
  }

  // Fecha (yyyy-MM-dd) que no sea posterior a hoy
  static notFutureDate(control: AbstractControl): ValidationErrors | null {
    const value = control.value;

    if (typeof value !== 'string' || value === '') return null;

    // Las fechas ISO se comparan bien como texto
    return value > toIsoDate(new Date()) ? { futureDate: true } : null;
  }
}
