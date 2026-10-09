import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { catchError, map, Observable, of, switchMap, timer } from 'rxjs';

// Valida contra el backend que un valor no esté repetido.
// - exists: consulta al backend y responde true si el valor ya está en uso
// - entity: cómo nombrar lo repetido en el mensaje ("un proveedor")
export function uniqueValue(
  exists: (value: string) => Observable<boolean>,
  entity: string,
  debounceMs = 400,
): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const value = typeof control.value === 'string' ? control.value.trim() : '';
    if (!value) return of(null);

    // Angular cancela la ejecución anterior si el valor cambia: el timer funciona como debounce
    return timer(debounceMs).pipe(
      switchMap(() => exists(value)),
      map((taken) => (taken ? { duplicated: { entity } } : null)),
      // Si la consulta falla no bloqueamos al usuario: el backend vuelve a validar al guardar
      catchError(() => of(null)),
    );
  };
}
