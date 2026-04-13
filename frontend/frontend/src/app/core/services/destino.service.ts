import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Destino } from '../models/destino.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class DestinoService {
  private readonly baseUrl = `${environment.apiUrl}/api/private/destinos`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Destino[]> {
    return this.http
      .get<ApiResponse<Destino[]>>(this.baseUrl, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  findById(id: number): Observable<Destino> {
    return this.http
      .get<ApiResponse<Destino>>(`${this.baseUrl}/${id}`, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  create(destino: Destino): Observable<{ status: string; message?: string }> {
    return this.http.post<{ status: string; message?: string }>(
      this.baseUrl,
      destino,
      { withCredentials: true }
    );
  }

  update(id: number, destino: Destino): Observable<{ status: string; message?: string }> {
    return this.http.put<{ status: string; message?: string }>(
      `${this.baseUrl}/${id}`,
      destino,
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