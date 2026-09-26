import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Movement } from '../interfaces/movement.interface';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../shared/interfaces/page-response.interface';

@Injectable({ providedIn: 'root' })
export class MovementService {
  private http = inject(HttpClient);

  // Obtiene una pagina de movimientos (mas recientes primero) por cuenta corriente
  getMovementsByCurrentAccount(currentAccountId: number, page: number, size: number) {
    return this.http.get<PageResponse<Movement>>(`${environment.apiUrl}/movements`, {
      params: { currentAccountId, page, size },
    });
  }
}
