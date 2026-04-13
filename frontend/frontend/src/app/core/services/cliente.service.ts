import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cliente } from '../models/cliente.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private readonly apiUrl = `${environment.apiUrl}/api/private/clientes`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Cliente[]> {
    return this.http.get<ApiResponse<Cliente[]>>(this.apiUrl, {
      withCredentials: true
    }).pipe(
      map((response) => response.data)
    );
  }

  findById(id: number): Observable<Cliente> {
    return this.http.get<ApiResponse<Cliente>>(`${this.apiUrl}/${id}`, {
      withCredentials: true
    }).pipe(
      map((response) => response.data)
    );
  }

  create(cliente: Cliente): Observable<any> {
    return this.http.post(this.apiUrl, cliente, {
      withCredentials: true
    });
  }

  update(id: number, cliente: Cliente): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, cliente, {
      withCredentials: true
    });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, {
      withCredentials: true
    });
  }
}