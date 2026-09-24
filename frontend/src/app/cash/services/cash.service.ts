import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CashDay } from '../interfaces/cash.interface';
import { environment } from '../../../environments/environment';
import { catchError, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CashService {
  private http = inject(HttpClient);

  // Obtiene la caja por dia dentro de un rango de fecha (yyyy-MM-dd)
  getCash(from: string, to: string) {
    return this.http.get<CashDay[]>(`${environment.apiUrl}/cash`, {
      params: { from, to },
    });
  }

  // Saldo actual de la caja
  getBalance() {
    return this.http
      .get<number>(`${environment.apiUrl}/cash/balance`)
      .pipe(catchError(() => of(0)));
  }
}
