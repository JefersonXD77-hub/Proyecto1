import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PaqueteAltaDemanda, PaqueteTuristico } from '../models/paquete-turistico.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class PaqueteService {
  private readonly baseUrl = `${environment.apiUrl}/api/private/paquetes`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<PaqueteTuristico[]> {
    return this.http
      .get<ApiResponse<PaqueteTuristico[]>>(this.baseUrl, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  findById(id: number): Observable<PaqueteTuristico> {
    return this.http
      .get<ApiResponse<PaqueteTuristico>>(`${this.baseUrl}/${id}`, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  findAltaDemanda(): Observable<PaqueteAltaDemanda[]> {
    return this.http
      .get<ApiResponse<PaqueteAltaDemanda[]>>(`${this.baseUrl}/alta-demanda`, { withCredentials: true })
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