import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FiltrosReporte, ReporteResponse, TipoReporte } from '../models/reporte.model';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private readonly baseUrl = `${environment.apiUrl}/api/private/reportes`;
  private readonly pdfBaseUrl = `${environment.apiUrl}/api/private/reportes/pdf`;

  constructor(private http: HttpClient) {}

  consultar<T = any>(tipo: TipoReporte, filtros: FiltrosReporte): Observable<ReporteResponse<T>> {
    let params = new HttpParams();

    if (filtros.fechaInicio) {
      params = params.set('fechaInicio', filtros.fechaInicio);
    }

    if (filtros.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }

    return this.http.get<ReporteResponse<T>>(
      `${this.baseUrl}/${tipo}`,
      {
        params,
        withCredentials: true
      }
    );
  }

  getPdfUrl(tipo: TipoReporte, filtros: FiltrosReporte): string {
    const params = new URLSearchParams();

    if (filtros.fechaInicio) {
      params.set('fechaInicio', filtros.fechaInicio);
    }

    if (filtros.fechaFin) {
      params.set('fechaFin', filtros.fechaFin);
    }

    const query = params.toString();
    return `${this.pdfBaseUrl}/${tipo}${query ? `?${query}` : ''}`;
  }
}