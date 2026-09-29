import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Dashboard } from '../interfaces/dashboard.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  // Datos del panel de inicio
  getDashboard() {
    return this.http.get<Dashboard>(`${environment.apiUrl}/dashboard`);
  }
}
