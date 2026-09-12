import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Movement } from '../interfaces/movement.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MovementService {
  private http = inject(HttpClient);

  // Obtener los movimiento (ventas + pagos) de una sucursal
  getMovementsByButcherShop(butcherShopId: number) {
    return this.http.get<Movement[]>(`${environment.apiUrl}/movements`, {
      params: { butcherShopId },
    });
  }
}
