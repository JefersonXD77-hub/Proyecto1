import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Reservacion } from '../models/reservacion.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ReservacionService {
  private readonly apiUrl = `${environment.apiUrl}/api/private/reservaciones`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Reservacion[]> {
    return this.http.get<ApiResponse<Reservacion[]>>(this.apiUrl, {
      withCredentials: true
    }).pipe(
      map(response => response.data)
    );
  }

  create(body: { idPaquete: number; fechaViaje: string; pasajeros: number[] }): Observable<any> {
    return this.http.post(this.apiUrl, body, {
      withCredentials: true
    });
  }
}