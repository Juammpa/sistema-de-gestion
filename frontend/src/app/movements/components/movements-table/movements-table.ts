import { Component } from '@angular/core';
import { PaymentItem } from '../payment-item/payment-item';
import { SaleItem } from '../sale-item/sale-item';

@Component({
  selector: 'movements-table',
  imports: [PaymentItem, SaleItem],
  templateUrl: './movements-table.html',
})
export class MovementsTable {}
