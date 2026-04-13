import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';

import {
  ErrorCargaMasiva,
  RespuestaCargaMasiva,
  ResumenCargaMasiva
} from '../../../core/models/carga-masiva.model';
import { CargaMasivaService } from '../../../core/services/carga-masiva.service';

@Component({
  selector: 'app-carga-masiva',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './carga-masiva.component.html',
  styleUrls: ['./carga-masiva.component.css']
})
export class CargaMasivaComponent {
  archivoSeleccionado: File | null = null;
  contenidoArchivo = '';

  loading = false;
  error = '';
  success = '';

  resumen: ResumenCargaMasiva | null = null;
  errores: ErrorCargaMasiva[] = [];

  constructor(private cargaMasivaService: CargaMasivaService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length > 0 ? input.files[0] : null;

    this.archivoSeleccionado = null;
    this.contenidoArchivo = '';
    this.error = '';
    this.success = '';
    this.resumen = null;
    this.errores = [];

    if (!file) {
      return;
    }

    if (!file.name.toLowerCase().endsWith('.txt')) {
      this.error = 'Debe seleccionar un archivo .txt';
      input.value = '';
      return;
    }

    this.archivoSeleccionado = file;

    const reader = new FileReader();

    reader.onload = () => {
      this.contenidoArchivo = typeof reader.result === 'string' ? reader.result : '';
    };

    reader.onerror = () => {
      this.error = 'No se pudo leer el archivo seleccionado';
      this.archivoSeleccionado = null;
      this.contenidoArchivo = '';
      input.value = '';
    };

    reader.readAsText(file, 'UTF-8');
  }

  submit(): void {
    if (!this.archivoSeleccionado) {
      this.error = 'Debe seleccionar un archivo .txt';
      return;
    }

    if (!this.contenidoArchivo.trim()) {
      this.error = 'El archivo está vacío o no pudo leerse correctamente';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    this.resumen = null;
    this.errores = [];

    this.cargaMasivaService.procesarContenido(this.contenidoArchivo)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (response: RespuestaCargaMasiva) => {
          this.success = response?.message || 'Carga procesada correctamente';
          this.resumen = response.resumen;
          this.errores = response.errores || [];
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al procesar carga masiva';
        }
      });
  }

  reset(): void {
    this.archivoSeleccionado = null;
    this.contenidoArchivo = '';
    this.loading = false;
    this.error = '';
    this.success = '';
    this.resumen = null;
    this.errores = [];

    const input = document.getElementById('archivoCargaMasiva') as HTMLInputElement | null;
    if (input) {
      input.value = '';
    }
  }

  get registrosInsertados() {
    return this.resumen?.registrosInsertados ?? null;
  }
}