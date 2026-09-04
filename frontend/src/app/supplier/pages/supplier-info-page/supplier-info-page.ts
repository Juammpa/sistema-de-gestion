import { Component, inject } from '@angular/core';
import { SupplierService } from '../../services/supplier.service';
import { ActivatedRoute } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { SupplierHeader } from './supplier-header/supplier-header';

@Component({
  selector: 'supplier-info-page',
  imports: [SupplierHeader],
  templateUrl: './supplier-info-page.html',
})
export default class SupplierInfoPage {
  private supplierService = inject(SupplierService);

  // Obtenemos el id del proveedor
  supplierId = inject(ActivatedRoute).snapshot.params['id'];

  supplierResource = rxResource({
    stream: () => this.supplierService.getSupplierById(this.supplierId),
  });
}
