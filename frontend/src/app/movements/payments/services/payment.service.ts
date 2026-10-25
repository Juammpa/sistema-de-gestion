import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { Payment, PaymentRequest } from '../interfaces/payment.interface';
import { notifyAlways } from '../../../core/errors/error-context';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private http = inject(HttpClient);

  // Crea un pago
  createPayment(payment: PaymentRequest) {
    return this.http.post<Payment>(`${environment.apiUrl}/payment`, payment);
  }

  // Obtiene un pago por Id
  getPaymentById(id: number) {
    return this.http.get<Payment>(`${environment.apiUrl}/payment/${id}`, {
      context: notifyAlways(),
    });
  }

  // Actualiza los datos de un pago
  updatePayment(id: number, payment: PaymentRequest) {
    return this.http.put(`${environment.apiUrl}/payment/${id}`, payment);
  }

  // Elimina un pago existente
  deletePaymentById(id: number) {
    return this.http.delete(`${environment.apiUrl}/payment/${id}`);
  }

  // Registra el rechazo de un cheque (suma el monto de vuelta a la deuda)
  rejectCheck(id: number) {
    return this.http.post<Payment>(`${environment.apiUrl}/payment/${id}/rejection`, null);
  }
}
