import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { SpecieValue } from '../../cut-meat/utils/specie.utils';
import { PurchaseType, PurchaseTypeRequest } from '../interfaces/purchase-type.interface';
import { environment } from '../../../environments/environment';
import { CachedRequest } from '../../shared/utils/cached-request';

@Injectable({ providedIn: 'root' })
export class PurchaseTypeService {
  private http = inject(HttpClient);

  // La lista completa la piden los ajustes y los modales de compra: se guarda para no pedirla cada vez
  private allTypes = new CachedRequest(() =>
    this.http.get<PurchaseType[]>(`${environment.apiUrl}/purchase-types`),
  );

  // Tipos de compra del usuario (activos y archivados). Sin especie devuelve ambas
  getAll(specie?: SpecieValue) {
    // Filtrada por especie no se guarda: se pide siempre al servidor
    if (specie) {
      return this.http.get<PurchaseType[]>(`${environment.apiUrl}/purchase-types`, {
        params: new HttpParams().set('specie', specie),
      });
    }

    return this.allTypes.get();
  }

  create(request: PurchaseTypeRequest) {
    return this.http
      .post<PurchaseType>(`${environment.apiUrl}/purchase-types`, request)
      .pipe(tap(() => this.allTypes.invalidate()));
  }

  update(id: number, request: PurchaseTypeRequest) {
    return this.http
      .put<PurchaseType>(`${environment.apiUrl}/purchase-types/${id}`, request)
      .pipe(tap(() => this.allTypes.invalidate()));
  }

  // Archiva el tipo (borrado logico)
  deleteById(id: number) {
    return this.http
      .delete(`${environment.apiUrl}/purchase-types/${id}`)
      .pipe(tap(() => this.allTypes.invalidate()));
  }

  activate(id: number) {
    return this.http
      .patch(`${environment.apiUrl}/purchase-types/${id}/activate`, {})
      .pipe(tap(() => this.allTypes.invalidate()));
  }

  // Indica si ya hay un tipo activo con ese nombre y especie (excludeId: el que se edita)
  existsByNameAndSpecie(name: string, specie: string, excludeId?: number) {
    let params = new HttpParams().set('name', name).set('specie', specie);
    if (excludeId !== undefined) params = params.set('excludeId', excludeId);

    return this.http.get<boolean>(`${environment.apiUrl}/purchase-types/exists`, { params });
  }
}
