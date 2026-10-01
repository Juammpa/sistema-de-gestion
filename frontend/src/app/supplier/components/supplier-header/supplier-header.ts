import { Component, computed, input, output, signal } from '@angular/core';
import { WhatsappButton } from '../../../shared/ui/whatsapp-button/whatsapp-button';
import { BackButton } from '../../../shared/ui/back-button/back-button';
import { Supplier } from '../../interfaces/supplier.interface';
import { CurrencyPipe } from '@angular/common';
import { AddPaymentButton } from '../../../movements/payments/components/add-payment-button/add-payment-button';
import { PaymentFormModal } from '../../../movements/payments/components/payment-form-modal/payment-form-modal';
import { AddPurchaseButton } from '../../../movements/purchases/components/add-purchase-button/add-purchase-button';
import { PurchaseFormModal } from '../../../movements/purchases/components/purchase-form-modal/purchase-form-modal';
import { SUPPLIER_TYPE_INFO } from '../../utils/supplier-type.utils';

@Component({
  selector: 'supplier-header',
  imports: [
    BackButton,
    WhatsappButton,
    CurrencyPipe,
    AddPaymentButton,
    PaymentFormModal,
    AddPurchaseButton,
    PurchaseFormModal,
  ],
  templateUrl: './supplier-header.html',
})
export class SupplierHeader {
  // Inputs
  supplier = input.required<Supplier>();

  // Outputs
  edit = output();
  remove = output();
  paymentCreated = output();
  purchaseCreated = output();

  // Señales
  showPurchaseModal = signal<boolean>(false);
  showPaymentModal = signal<boolean>(false);

  // Saldo a mostrar
  displayedBalance = computed(() => this.supplier().currentBalance);

  // Datos para mostrar el tipo de proveedor
  typeInfo = computed(() => SUPPLIER_TYPE_INFO[this.supplier().supplierType]);
}
