import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Supplier } from '../interfaces/supplier.interface';
import { environment } from '../../../environments/environment';
import { catchError, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SupplierService {
  private http = inject(HttpClient);

  // Obtener todos los proveedores
  getAllSuppliers() {
    return this.http
      .get<Supplier[]>(`${environment.apiUrl}/suppliers`)
      .pipe(catchError(() => of([])));
  }

  // Obtener proveedor por Id
  getSupplierById(id: number) {
    return this.http.get<Supplier>(`${environment.apiUrl}/suppliers/${id}`);
  }
}
