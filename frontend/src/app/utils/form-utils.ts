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

        case 'minlength':
          return `Minimo de ${errors['minlength'].requiredLength} carácteres`;

        case 'min':
          return `El valor minimo es ${errors['min'].min}`;
      }
    }

    return null;
  }

  // Valida campo del formulario
  static isValidField(form: FormGroup, fieldName: string): boolean | null {
    return form.controls[fieldName].errors && form.controls[fieldName].touched;
  }

  // Retorna el mensaje descriptivo
  static getFieldError(form: FormGroup, fieldName: string): string | null {
    if (!form.controls[fieldName]) return null;

    // Obtenemos los errores (si hay)
    const errors = form.controls[fieldName].errors ?? {};

    return FormUtils.getTextError(errors);
  }
}
