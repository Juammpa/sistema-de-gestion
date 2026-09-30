import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Client, ClientCreateRequest, ClientRequest } from '../interfaces/client.interface';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ClientService {
  // Inyectamos HttpClient
  http = inject(HttpClient);

  // Obtener todos los clientes
  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${environment.apiUrl}/clients`);
  }

  // Obtiene un cliente por Id
  getClientById(id: number) {
    return this.http.get<Client>(`${environment.apiUrl}/clients/${id}`);
  }

  // Crea un nuevo cliente
  createClient(client: ClientCreateRequest) {
    return this.http.post<Client>(`${environment.apiUrl}/clients`, client);
  }

  // Actualiza datos de un cliente
  updateClient(id: number, client: ClientRequest) {
    return this.http.put<Client>(`${environment.apiUrl}/clients/${id}`, client);
  }

  // Elimina un cliente por Id
  deleteClient(id: number) {
    return this.http.delete(`${environment.apiUrl}/clients/${id}`);
  }
}
