import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AccountDeletionService {
  private http = inject(HttpClient);

  // Elimina la cuenta y todos sus datos.
  // 'confirmation' es el nombre de usuario
  deleteAccount(confirmation: string) {
    return this.http.post<void>(`${environment.apiUrl}/account/deletion`, { confirmation });
  }
}
