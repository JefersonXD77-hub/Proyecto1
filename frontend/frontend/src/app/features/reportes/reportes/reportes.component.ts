import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import {
  FiltrosReporte,
  ReporteOption,
  TipoReporte
} from '../../../core/models/reporte.model';
import { ReporteService } from '../../../core/services/reporte.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reportes.component.html',
  styleUrls: ['./reportes.component.css']
})
export class ReportesComponent {
  loading = false;
  error = '';
  success = '';

  tipoReporteSeleccionado: TipoReporte = 'ventas';
  resultado: any = null;

  form: FormGroup;

  readonly opcionesReporte: ReporteOption[] = [
    { value: 'ventas', label: 'Reporte de ventas' },
    { value: 'cancelaciones', label: 'Reporte de cancelaciones' },
    { value: 'ganancias', label: 'Reporte de ganancias' },
    { value: 'agente-mas-ventas', label: 'Agente con más ventas' },
    { value: 'agente-mas-ganancias', label: 'Agente con más ganancias' },
    { value: 'paquete-mas-vendido', label: 'Paquete más vendido' },
    { value: 'paquete-menos-vendido', label: 'Paquete menos vendido' },
    { value: 'ocupacion-destino', label: 'Ocupación por destino' }
  ];

  constructor(
    private fb: FormBuilder,
    private reporteService: ReporteService
  ) {
    this.form = this.fb.group({
      tipo: ['ventas'],
      fechaInicio: [''],
      fechaFin: ['']
    });
  }

  submit(): void {
    const raw = this.form.getRawValue();

    this.tipoReporteSeleccionado = raw.tipo as TipoReporte;

    const filtros: FiltrosReporte = {
      fechaInicio: raw.fechaInicio || null,
      fechaFin: raw.fechaFin || null
    };

    this.loading = true;
    this.error = '';
    this.success = '';
    this.resultado = null;

    this.reporteService.consultar(this.tipoReporteSeleccionado, filtros)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (response) => {
          this.resultado = response.data;
          this.success = 'Reporte generado correctamente';
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al generar reporte';
        }
      });
  }

  exportPdf(): void {
    const raw = this.form.getRawValue();

    const filtros: FiltrosReporte = {
      fechaInicio: raw.fechaInicio || null,
      fechaFin: raw.fechaFin || null
    };

    const url = this.reporteService.getPdfUrl(raw.tipo as TipoReporte, filtros);
    window.open(url, '_blank');
  }

  reset(): void {
    this.form.reset({
      tipo: 'ventas',
      fechaInicio: '',
      fechaFin: ''
    });

    this.tipoReporteSeleccionado = 'ventas';
    this.resultado = null;
    this.error = '';
    this.success = '';
  }

  isArrayResult(): boolean {
    return Array.isArray(this.resultado);
  }

  getArrayHeaders(): string[] {
    if (!Array.isArray(this.resultado) || this.resultado.length === 0) {
      return [];
    }

    return Object.keys(this.resultado[0]);
  }

  getObjectKeys(obj: any): string[] {
    if (!obj || Array.isArray(obj)) {
      return [];
    }

    return Object.keys(obj);
  }

  hasNestedReservaciones(): boolean {
    return !!this.resultado && !Array.isArray(this.resultado) && Array.isArray(this.resultado.reservaciones);
  }

  getNestedReservacionesHeaders(): string[] {
    if (!this.hasNestedReservaciones() || this.resultado.reservaciones.length === 0) {
      return [];
    }

    return Object.keys(this.resultado.reservaciones[0]);
  }

  formatValue(value: any): string {
    if (value === null || value === undefined || value === '') {
      return '—';
    }

    return String(value);
  }
}