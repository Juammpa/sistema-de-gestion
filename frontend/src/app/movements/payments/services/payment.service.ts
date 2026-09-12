import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { Payment, PaymentRequest } from '../interfaces/payment.interface';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private http = inject(HttpClient);

  // Crea un pago
  createPayment(payment: PaymentRequest) {
    return this.http.post<Payment>(`${environment.apiUrl}/payment`, payment);
  }

  // Obtiene todos los pagos de un cliente
  getPaymentsByClient(clientId: number) {
    return this.http.get<Payment[]>(`${environment.apiUrl}/payment`, { params: { clientId } });
  }

  // Obtiene un pago por Id
  getPaymentById(id: number) {
    return this.http.get<Payment>(`${environment.apiUrl}/payment/${id}`);
  }

  // Actualiza los datos de un pago
  updatePayment(id: number, payment: PaymentRequest) {
    return this.http.put(`${environment.apiUrl}/payment/${id}`, payment);
  }

  // Elimina un pago existente
  deletePaymentById(id: number) {
    return this.http.delete(`${environment.apiUrl}/payment/${id}`);
  }
}
