import { FormGroup, ValidationErrors } from '@angular/forms';

export class FormUtils {
  // Textos descriptivos del error
  static getTextError(errors: ValidationErrors): string | null {
    // Obtenemos los errores
    for (const key of Object.keys(errors)) {
      // Por cada error, retornamos un mensaje
      switch (key) {
        case 'required':
          return 'Este campo es requerido';

        case 'blank':
          return 'No puede contener solo espacios';

        case 'minlength':
          return `Mínimo de ${errors['minlength'].requiredLength} caracteres`;

        case 'maxlength':
          return `Máximo de ${errors['maxlength'].requiredLength} caracteres`;

        case 'min':
          return `El valor mínimo es ${errors['min'].min}`;

        case 'max':
          return `El valor máximo es ${errors['max'].max}`;

        case 'personName':
          return 'Solo se permiten letras, espacios, apóstrofes, puntos y guiones';

        case 'phoneNumber':
          return 'Formato inválido. Ej: +54 9 3442 123456';

        case 'pattern':
          return 'El formato es inválido';
      }
    }

    return null;
  }

  // Valida campo del formulario
  static isValidField(form: FormGroup, fieldName: string): boolean | null {
    const control = form.controls[fieldName];
    return control && control.errors && control.touched;
  }

  // Retorna el mensaje descriptivo
  static getFieldError(form: FormGroup, fieldName: string): string | null {
    if (!form.controls[fieldName]) return null;

    // Obtenemos los errores (si hay)
    const errors = form.controls[fieldName].errors ?? {};

    return FormUtils.getTextError(errors);
  }

  // Quita los espacios sobrantes de los textos antes de enviar al backend
  static trimValues<T extends Record<string, unknown>>(value: T): T {
    return Object.fromEntries(
      Object.entries(value).map(([key, val]) => [key, typeof val === 'string' ? val.trim() : val]),
    ) as T;
  }
}
