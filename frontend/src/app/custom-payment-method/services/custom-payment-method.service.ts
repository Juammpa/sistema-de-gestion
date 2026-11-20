import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { tap } from 'rxjs';
import {
  CustomPaymentMethod,
  CustomPaymentMethodRequest,
} from '../interfaces/custom-payment-method.interface';
import { environment } from '../../../environments/environment';
import { CachedRequest } from '../../shared/utils/cached-request';

@Injectable({ providedIn: 'root' })
export class CustomPaymentMethodService {
  private http = inject(HttpClient);

  // La lista la piden los ajustes y los modales de pago: se guarda para no pedirla cada vez
  private allMethods = new CachedRequest(() =>
    this.http.get<CustomPaymentMethod[]>(`${environment.apiUrl}/payment-methods`),
  );

  // Metodos de pago del usuario (activos y archivados)
  getAll() {
    return this.allMethods.get();
  }

  create(request: CustomPaymentMethodRequest) {
    return this.http
      .post<CustomPaymentMethod>(`${environment.apiUrl}/payment-methods`, request)
      .pipe(tap(() => this.allMethods.invalidate()));
  }

  update(id: number, request: CustomPaymentMethodRequest) {
    return this.http
      .put<CustomPaymentMethod>(`${environment.apiUrl}/payment-methods/${id}`, request)
      .pipe(tap(() => this.allMethods.invalidate()));
  }

  // Archivo el metodo (borrado logico)
  deleteById(id: number) {
    return this.http
      .delete(`${environment.apiUrl}/payment-methods/${id}`)
      .pipe(tap(() => this.allMethods.invalidate()));
  }

  activate(id: number) {
    return this.http
      .patch(`${environment.apiUrl}/payment-methods/${id}/activate`, {})
      .pipe(tap(() => this.allMethods.invalidate()));
  }

  // Indica si el nombre ya lo usa un metodo activo o uno de fabrica (excludeId: el que se edita)
  existsByName(name: string, excludeId?: number) {
    let params = new HttpParams().set('name', name);

    if (excludeId !== undefined) params = params.set('excludeId', excludeId);

    return this.http.get<boolean>(`${environment.apiUrl}/payment-methods/exists`, { params });
  }
}
