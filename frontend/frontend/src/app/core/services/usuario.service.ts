import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Rol } from '../models/rol.model';
import { Usuario } from '../models/usuario.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private readonly baseUrl = `${environment.apiUrl}/api/private/usuarios`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Usuario[]> {
    return this.http
      .get<ApiResponse<Usuario[]>>(this.baseUrl, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  findById(id: number): Observable<Usuario> {
    return this.http
      .get<ApiResponse<Usuario>>(`${this.baseUrl}/${id}`, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  findRoles(): Observable<Rol[]> {
    return this.http
      .get<ApiResponse<Rol[]>>(`${this.baseUrl}/roles`, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  create(body: any): Observable<{ status: string; message?: string }> {
    return this.http.post<{ status: string; message?: string }>(
      this.baseUrl,
      body,
      { withCredentials: true }
    );
  }

  update(id: number, body: any): Observable<{ status: string; message?: string }> {
    return this.http.put<{ status: string; message?: string }>(
      `${this.baseUrl}/${id}`,
      body,
      { withCredentials: true }
    );
  }

  delete(id: number): Observable<{ status: string; message?: string }> {
    return this.http.delete<{ status: string; message?: string }>(
      `${this.baseUrl}/${id}`,
      { withCredentials: true }
    );
  }
}