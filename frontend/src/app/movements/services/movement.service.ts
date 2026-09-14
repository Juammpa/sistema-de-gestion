import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Movement } from '../interfaces/movement.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MovementService {
  private http = inject(HttpClient);

  // Obtener los movimientos (ventas + pagos) por cuenta corriente
  getMovementsByCurrentAccount(currentAccountId: number) {
    return this.http.get<Movement[]>(`${environment.apiUrl}/movements`, {
      params: { currentAccountId },
    });
  }
}
