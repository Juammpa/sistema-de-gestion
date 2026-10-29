import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import {
  CustomPaymentMethod,
  CustomPaymentMethodRequest,
} from '../interfaces/custom-payment-method.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CustomPaymentMethodService {
  private http = inject(HttpClient);

  // Metodos de pago del usuario (activos y archivados)
  getAll() {
    return this.http.get<CustomPaymentMethod[]>(`${environment.apiUrl}/payment-methods`);
  }

  create(request: CustomPaymentMethodRequest) {
    return this.http.post<CustomPaymentMethod>(`${environment.apiUrl}/payment-methods`, request);
  }

  update(id: number, request: CustomPaymentMethod) {
    return this.http.put<CustomPaymentMethod>(
      `${environment.apiUrl}/payment-methods/${id}`,
      request,
    );
  }

  // Archivo el metodo (borrado logico)
  deleteById(id: number) {
    return this.http.delete(`${environment.apiUrl}/payment-methods/${id}`);
  }

  activate(id: number) {
    return this.http.patch(`${environment.apiUrl}/payment-methods/${id}/activate`, {});
  }

  // Indica si el nombre ya lo usa un metodo activo o uno de fabrica (excludeId: el que se edita)
  existsByName(name: string, excludeId?: number) {
    let params = new HttpParams().set('name', name);

    if (excludeId !== undefined) params = params.set('excludeId', excludeId);

    return this.http.get<boolean>(`${environment.apiUrl}/payment-methods/exists`, { params });
  }
}
