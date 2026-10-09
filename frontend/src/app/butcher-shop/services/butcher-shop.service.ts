import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import {
  ButcherShop,
  ButcherShopRequest,
  ButcherShopUpdateRequest,
} from '../interfaces/butcher-shop.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ButcherShopService {
  private http = inject(HttpClient);

  // Crea una sucursal para un cliente existente
  createButcherShop(butcherShop: ButcherShopRequest) {
    return this.http.post<ButcherShop>(`${environment.apiUrl}/butcher-shops`, butcherShop);
  }

  // Edita los datos de una sucursal
  updateButcherShop(id: number, butcherShop: ButcherShopUpdateRequest) {
    return this.http.put<ButcherShop>(`${environment.apiUrl}/butcher-shops/${id}`, butcherShop);
  }

  // Elimina (logicamente) una sucursal: deja de mostrarse pero su historial se conserva.
  deleteButcherShop(id: number) {
    return this.http.delete<void>(`${environment.apiUrl}/butcher-shops/${id}`);
  }

  // Indica si el cliente ya tiene una sucursal activa con ese nombre
  // (excludeId: la que se edita)
  nameExists(clientId: number, name: string, excludeId?: number) {
    let params = new HttpParams().set('clientId', clientId).set('name', name);

    if (excludeId !== undefined) params = params.set('excludeId', excludeId);

    return this.http.get<boolean>(`${environment.apiUrl}/butcher-shops/exists`, { params });
  }
}
