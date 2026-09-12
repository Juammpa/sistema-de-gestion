import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sale, SaleRequest } from '../interfaces/sales.interface';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SaleService {
  // Inyectamos HttpClient
  private http = inject(HttpClient);

  // Crear una venta
  createSale(sale: SaleRequest) {
    return this.http.post<Sale>(`${environment.apiUrl}/sales`, sale);
  }

  // Obtener venta por Id
  getSaleById(id: number) {
    return this.http.get<Sale>(`${environment.apiUrl}/sales/${id}`);
  }

  // Editar venta existente
  updateSale(id: number, sale: SaleRequest) {
    return this.http.put<Sale>(`${environment.apiUrl}/sales/${id}`, sale);
  }

  // Obtener todas las ventas
  getAllSales() {
    return this.http.get<Sale[]>(`${environment.apiUrl}/all`);
  }

  // Obtener todas las ventas de un cliente
  getAllSalesByClient(clientId: number) {
    return this.http.get<Sale[]>(`${environment.apiUrl}`, { params: { clientId } });
  }

  // Eliminar venta por Id
  deleteSaleById(id: number) {
    return this.http.delete(`${environment.apiUrl}/${id}`);
  }
}
