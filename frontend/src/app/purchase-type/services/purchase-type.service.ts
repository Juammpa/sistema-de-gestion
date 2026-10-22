import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SpecieValue } from '../../cut-meat/utils/specie.utils';
import { PurchaseType } from '../interfaces/purchase-type.interface';
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
}
