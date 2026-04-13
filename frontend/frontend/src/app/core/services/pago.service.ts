import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagoReservacionResponse } from '../models/pago.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  resumen?: any;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class PagoService {
  private readonly apiUrl = `${environment.apiUrl}/api/private/pagos`;

  constructor(private http: HttpClient) {}

  findByReservacion(idReservacion: number): Observable<PagoReservacionResponse> {
    return this.http.get<any>(`${this.apiUrl}/reservacion/${idReservacion}`, {
      withCredentials: true
    }).pipe(
      map((response) => ({
        resumen: response.resumen,
        data: response.data
      }))
    );
  }

  create(body: {
    idReservacion: number;
    idMetodoPago: number;
    monto: number;
    fechaPago?: string;
  }): Observable<any> {
    return this.http.post(this.apiUrl, body, {
      withCredentials: true
    });
  }

  getComprobanteUrl(idReservacion: number): string {
    return `${environment.apiUrl}/api/private/pagos/comprobante/${idReservacion}`;
  }
}