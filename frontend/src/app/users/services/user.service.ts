import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../shared/interfaces/page-response.interface';
import { User, UserCreateRequest } from '../interfaces/user.interface';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  // Una pagina de usuarios. "search" filtra por usuario, nombre o correo
  getUsers(search: string, page: number, size: number) {
    let params = new HttpParams().set('page', page).set('size', size);

    if (search) params = params.set('search', search);

    return this.http.get<PageResponse<User>>(`${environment.apiUrl}/admin/users`, { params });
  }

  // Crea un usuario con contraseña temporal (se cambia al primer ingreso)
  createUser(user: UserCreateRequest) {
    return this.http.post<User>(`${environment.apiUrl}/admin/users`, user);
  }
}
