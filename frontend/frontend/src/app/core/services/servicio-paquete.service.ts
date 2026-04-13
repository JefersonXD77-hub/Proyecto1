import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResumenCostosPaquete, ServicioPaquete } from '../models/servicio-paquete.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ServicioPaqueteService {
  private readonly baseUrl = `${environment.apiUrl}/api/private/servicios-paquete`;

  constructor(private http: HttpClient) {}

  findByPaquete(idPaquete: number): Observable<ServicioPaquete[]> {
    return this.http
      .get<ApiResponse<ServicioPaquete[]>>(`${this.baseUrl}/paquete/${idPaquete}`, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  getResumenCostos(idPaquete: number): Observable<ResumenCostosPaquete> {
    return this.http
      .get<ApiResponse<ResumenCostosPaquete>>(`${this.baseUrl}/resumen/${idPaquete}`, { withCredentials: true })
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