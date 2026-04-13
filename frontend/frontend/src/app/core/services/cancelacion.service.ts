import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cancelacion } from '../models/cancelacion.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class CancelacionService {
  private readonly apiUrl = `${environment.apiUrl}/api/private/cancelaciones`;

  constructor(private http: HttpClient) {}

  findByReservacion(idReservacion: number): Observable<Cancelacion> {
    return this.http.get<ApiResponse<Cancelacion>>(`${this.apiUrl}/${idReservacion}`, {
      withCredentials: true
    }).pipe(
      map(response => response.data)
    );
  }

  create(idReservacion: number): Observable<any> {
    return this.http.post(this.apiUrl, { idReservacion }, {
      withCredentials: true
    });
  }
}