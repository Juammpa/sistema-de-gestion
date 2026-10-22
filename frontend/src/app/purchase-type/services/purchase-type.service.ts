import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SpecieValue } from '../../cut-meat/utils/specie.utils';
import { PurchaseType, PurchaseTypeRequest } from '../interfaces/purchase-type.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PurchaseTypeService {
  private http = inject(HttpClient);

  // Tipos de compra del usuario (activos y archivados). Sin especie devuelve ambas
  getAll(specie?: SpecieValue) {
    let params = new HttpParams();
    if (specie) params = params.set('specie', specie);

    return this.http.get<PurchaseType[]>(`${environment.apiUrl}/purchase-types`, { params });
  }

  create(request: PurchaseTypeRequest) {
    return this.http.post<PurchaseType>(`${environment.apiUrl}/purchase-types`, request);
  }

  update(id: number, request: PurchaseTypeRequest) {
    return this.http.put<PurchaseType>(`${environment.apiUrl}/purchase-types/${id}`, request);
  }

  // Archiva el tipo (borrado logico)
  deleteById(id: number) {
    return this.http.delete(`${environment.apiUrl}/purchase-types/${id}`);
  }

  activate(id: number) {
    return this.http.patch(`${environment.apiUrl}/purchase-types/${id}/activate`, {});
  }

  // Indica si ya hay un tipo activo con ese nombre y especie (excludeId: el que se edita)
  existsByNameAndSpecie(name: string, specie: string, excludeId?: number) {
    let params = new HttpParams().set('name', name).set('specie', specie);
    if (excludeId !== undefined) params = params.set('excludeId', excludeId);

    return this.http.get<boolean>(`${environment.apiUrl}/purchase-types/exists`, { params });
  }
}
