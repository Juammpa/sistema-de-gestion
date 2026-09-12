import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CutMeat, CutMeatRequest } from '../interfaces/cut-meat.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CutMeatService {
  private http = inject(HttpClient);

  // Craer un tipo de corte de carne
  createCutMeat(cutMeat: CutMeatRequest) {
    return this.http.post<CutMeat>(`${environment.apiUrl}/cut-meat`, cutMeat);
  }

  // Obtener un tipo de corte de corte
  getCutMeatById(id: number) {
    return this.http.get<CutMeat>(`${environment.apiUrl}/cut-meat/${id}`);
  }

  // Obtener todos los cortes de carne
  getAllCutMeats() {
    return this.http.get<CutMeat[]>(`${environment.apiUrl}/cut-meat`);
  }

  // Actualizar un tipo de corte de carne
  updateCutMeat(id: number, cutMeat: CutMeatRequest) {
    return this.http.put<CutMeat>(`${environment.apiUrl}/cut-meat/${id}`, cutMeat);
  }

  // Eliminar un tipo de corte de carne
  deleteCutMeatById(id: number) {
    return this.http.delete(`${environment.apiUrl}/cut-meat/${id}`);
  }
}
