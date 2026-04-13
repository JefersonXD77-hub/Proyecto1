import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RespuestaCargaMasiva } from '../models/carga-masiva.model';

@Injectable({
  providedIn: 'root'
})
export class CargaMasivaService {
  private readonly baseUrl = `${environment.apiUrl}/api/private/carga-masiva`;

  constructor(private http: HttpClient) {}

  procesarContenido(contenido: string): Observable<RespuestaCargaMasiva> {
    return this.http.post<RespuestaCargaMasiva>(
      this.baseUrl,
      contenido,
      {
        withCredentials: true,
        headers: new HttpHeaders({
          'Content-Type': 'text/plain'
        })
      }
    );
  }
}