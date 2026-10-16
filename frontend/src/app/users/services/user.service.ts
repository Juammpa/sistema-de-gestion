import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../shared/interfaces/page-response.interface';
import { User, UserCreateRequest, UserRole } from '../interfaces/user.interface';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/users`;

  // Una pagina de usuarios. "search" filtra por usuario, nombre o correo
  getUsers(search: string, page: number, size: number) {
    let params = new HttpParams().set('page', page).set('size', size);

    if (search) params = params.set('search', search);

    return this.http.get<PageResponse<User>>(this.baseUrl, { params });
  }

  // Crea un usuario con contraseña temporal (se cambia al primer ingreso)
  createUser(user: UserCreateRequest) {
    return this.http.post<User>(`${environment.apiUrl}/admin/users`, user);
  }

  // Cambia el rol
  changeRol(id: string, role: UserRole) {
    return this.http.patch<User>(`${this.baseUrl}/${id}/role`, { role });
  }

  // Deshabilita la cuenta: no se borra, sus datos se conservan
  disableUser(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // Vuelve a habilitar una cuenta deshabilitada
  activateUser(id: string) {
    return this.http.patch<void>(`${this.baseUrl}/${id}/activate`, {});
  }
}
