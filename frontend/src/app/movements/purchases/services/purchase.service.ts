import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Purchase, PurchaseRequest } from '../interfaces/purchase.interface';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PurchaseService {
  private http = inject(HttpClient);

  // Crear un nuevo pago
  createPurchase(purchase: PurchaseRequest) {
    return this.http.post<Purchase>(`${environment.apiUrl}/purchase`, purchase);
  }

  // Obtener una compra por Id
  getPurchaseById(id: number) {
    return this.http.get<Purchase>(`${environment.apiUrl}/purchase/${id}`);
  }

  // Actualizar una compra existente
  updatePurchase(id: number, purchase: PurchaseRequest) {
    return this.http.put<Purchase>(`${environment.apiUrl}/purchase/${id}`, purchase);
  }

  // Eliminar una compra existente
  deletePurchaseById(id: number) {
    return this.http.delete(`${environment.apiUrl}/purchase/${id}`);
  }
}
