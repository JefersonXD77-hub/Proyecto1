import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MetodoPago } from '../models/metodo-pago.model';

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class MetodoPagoService {
  private readonly apiUrl = `${environment.apiUrl}/api/private/metodos-pago`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<MetodoPago[]> {
    return this.http.get<ApiResponse<MetodoPago[]>>(this.apiUrl, {
      withCredentials: true
    }).pipe(
      map(response => response.data)
    );
  }
}