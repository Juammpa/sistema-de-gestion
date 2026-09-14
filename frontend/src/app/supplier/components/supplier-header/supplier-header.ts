import { Component, computed, input, output, signal } from '@angular/core';
import { WhatsappButton } from '../../../shared/ui/whatsapp-button/whatsapp-button';
import { BackButton } from '../../../shared/ui/back-button/back-button';
import { Supplier } from '../../interfaces/supplier.interface';
import { CurrencyPipe } from '@angular/common';
import { AddPaymentButton } from '../../../movements/payments/components/add-payment-button/add-payment-button';
import { PaymentFormModal } from '../../../movements/payments/components/payment-form-modal/payment-form-modal';

@Component({
  selector: 'supplier-header',
  imports: [BackButton, WhatsappButton, CurrencyPipe, AddPaymentButton, PaymentFormModal],
  templateUrl: './supplier-header.html',
})
export class SupplierHeader {
  // Inputs
  supplier = input.required<Supplier>();

  // Outputs
  edit = output();
  remove = output();
  paymentCreated = output();

  // Señales
  // showPurchaseModal = signal<boolean>(false);
  showPaymentModal = signal<boolean>(false);

  // Saldo a mostrar
  displayedBalance = computed(() => this.supplier().currentBalance);
}
