import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Proveedor } from '../models/proveedor.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ProveedorService {
  private readonly baseUrl = `${environment.apiUrl}/api/private/proveedores`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Proveedor[]> {
    return this.http
      .get<ApiResponse<Proveedor[]>>(this.baseUrl, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  findById(id: number): Observable<Proveedor> {
    return this.http
      .get<ApiResponse<Proveedor>>(`${this.baseUrl}/${id}`, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  create(proveedor: any): Observable<{ status: string; message?: string }> {
    return this.http.post<{ status: string; message?: string }>(
      this.baseUrl,
      proveedor,
      { withCredentials: true }
    );
  }

  update(id: number, proveedor: any): Observable<{ status: string; message?: string }> {
    return this.http.put<{ status: string; message?: string }>(
      `${this.baseUrl}/${id}`,
      proveedor,
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