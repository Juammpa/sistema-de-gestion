import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../shared/interfaces/page-response.interface';
import { User } from '../interfaces/user.interface';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  // Una pagina de usuarios. "search" filtra por usuario, nombre o correo
  getUsers(search: string, page: number, size: number) {
    let params = new HttpParams().set('page', page).set('size', size);

    if (search != null) params = params.set('search', search);

    return this.http.get<PageResponse<User>>(`${environment.apiUrl}/admin/users`, { params });
  }
}
