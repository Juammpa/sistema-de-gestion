import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { StockResponse } from '../interfaces/stock.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StockService {
  private http = inject(HttpClient);

  // Piezas de stock y resumen de un rango de fechas (yyyy-MM-dd)
  getStock(from: string, to: string) {
    return this.http.get<StockResponse>(`${environment.apiUrl}/stock`, {
      params: { from, to },
    });
  }

  // Reparte los kilos de una compra entre sus piezas (en orden).
  updatePieceWeights(purchaseId: number, kilograms: number[]) {
    return this.http.put<void>(`${environment.apiUrl}/stock/purchase/${purchaseId}/weights`, {
      kilograms,
    });
  }
}
