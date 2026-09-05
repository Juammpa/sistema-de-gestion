import { Component, inject, OnDestroy } from '@angular/core';
import { SupplierService } from '../../services/supplier.service';
import { ActivatedRoute } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { SupplierHeader } from './supplier-header/supplier-header';
import { SidebarService } from '../../../shared/components/sidebar/services/sidebar.service';

@Component({
  selector: 'supplier-info-page',
  imports: [SupplierHeader],
  templateUrl: './supplier-info-page.html',
})
export default class SupplierInfoPage implements OnDestroy {
  private supplierService = inject(SupplierService);
  private sidebarService = inject(SidebarService);

  // Contraemos el sidebar
  constructor() {
    this.sidebarService.collapse();
  }

  ngOnDestroy() {
    this.sidebarService.expand();
  }

  // Obtenemos el id del proveedor
  supplierId = inject(ActivatedRoute).snapshot.params['id'];

  supplierResource = rxResource({
    stream: () => this.supplierService.getSupplierById(this.supplierId),
  });
}
