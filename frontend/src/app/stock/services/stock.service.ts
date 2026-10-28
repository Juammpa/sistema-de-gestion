import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AvailableCut, StockCutRequest, StockResponse } from '../interfaces/stock.interface';
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

  // Reemplaza los cortes de una pieza (lista vacia = pieza sin cortes)
  updatePieceCuts(purchaseId: number, pieceNumber: number, cuts: StockCutRequest[]) {
    return this.http.put<void>(
      `${environment.apiUrl}/stock/purchase/${purchaseId}/pieces/${pieceNumber}/cuts`,
      { cuts },
    );
  }

  // Unidades sin vender de cortes activos, de la mas antigua a la mas nueva (FIFO)
  getAvailableCuts() {
    return this.http.get<AvailableCut[]>(`${environment.apiUrl}/stock/cuts/available`);
  }
}
