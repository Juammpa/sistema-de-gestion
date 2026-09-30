import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Supplier, SupplierRequest } from '../interfaces/supplier.interface';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SupplierService {
  private http = inject(HttpClient);

  // Obtener todos los proveedores
  getAllSuppliers() {
    return this.http.get<Supplier[]>(`${environment.apiUrl}/suppliers`);
  }

  // Obtener proveedor por Id
  getSupplierById(id: number) {
    return this.http.get<Supplier>(`${environment.apiUrl}/suppliers/${id}`);
  }

  // Crea un nuevo proveedor
  createSupplier(supplier: SupplierRequest) {
    return this.http.post<Supplier>(`${environment.apiUrl}/suppliers`, supplier);
  }

  // Actualiza datos de un proveedor
  updateSupplier(id: number, supplier: SupplierRequest) {
    return this.http.put<Supplier>(`${environment.apiUrl}/suppliers/${id}`, supplier);
  }

  // Elimina un proveedor por Id
  deleteSupplier(id: number) {
    return this.http.delete(`${environment.apiUrl}/suppliers/${id}`);
  }
}
