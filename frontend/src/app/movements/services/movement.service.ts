import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Movement, MovementType } from '../interfaces/movement.interface';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../shared/interfaces/page-response.interface';
import { DateRange } from '../../shared/interfaces/date-range.interface';

@Injectable({ providedIn: 'root' })
export class MovementService {
  private http = inject(HttpClient);

  // Obtiene una pagina de movimientos (mas recientes primero) por cuenta corriente.
  // type: null = todos los tipos. range: null en un lado = sin limite.
  getMovementsByCurrentAccount(
    currentAccountId: number,
    page: number,
    size: number,
    type: MovementType | null = null,
    range: DateRange = { from: null, to: null },
  ) {
    return this.http.get<PageResponse<Movement>>(`${environment.apiUrl}/movements`, {
      params: {
        currentAccountId,
        page,
        size,
        ...(type ? { type } : {}),
        ...(range.from ? { from: range.from } : {}),
        ...(range.to ? { to: range.to } : {}),
      },
    });
  }
}
