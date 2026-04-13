import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Reservacion } from '../../../core/models/reservacion.model';
import { Cancelacion } from '../../../core/models/cancelacion.model';
import { ReservacionService } from '../../../core/services/reservacion.service';
import { CancelacionService } from '../../../core/services/cancelacion.service';

@Component({
  selector: 'app-cancelaciones',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cancelaciones.component.html',
  styleUrls: ['./cancelaciones.component.css']
})
export class CancelacionesComponent implements OnInit {
  reservaciones: Reservacion[] = [];
  cancelacion: Cancelacion | null = null;

  selectedReservacionId: number | null = null;

  loadingReservaciones = false;
  loadingCancelacion = false;
  saving = false;

  error = '';
  success = '';

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private reservacionService: ReservacionService,
    private cancelacionService: CancelacionService
  ) {
    this.form = this.fb.group({
      idReservacion: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadReservaciones();
  }

  private resetEstadoVista(): void {
    this.cancelacion = null;
    this.error = '';
    this.success = '';
  }

  loadReservaciones(): void {
    this.loadingReservaciones = true;
    this.error = '';

    this.reservacionService.findAll()
      .pipe(
        finalize(() => {
          this.loadingReservaciones = false;
        })
      )
      .subscribe({
        next: (data: Reservacion[]) => {
          this.reservaciones = data.filter(r => r.activo !== false);
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al cargar reservaciones';
        }
      });
  }

  onReservacionChange(): void {
    const idReservacion = Number(this.form.get('idReservacion')?.value);
    this.selectedReservacionId = idReservacion || null;

    this.resetEstadoVista();

    if (!this.selectedReservacionId) {
      return;
    }

    this.loadCancelacionByReservacion(this.selectedReservacionId);
  }

  loadCancelacionByReservacion(idReservacion: number): void {
    this.loadingCancelacion = true;
    this.error = '';

    this.cancelacionService.findByReservacion(idReservacion)
      .pipe(
        finalize(() => {
          this.loadingCancelacion = false;
        })
      )
      .subscribe({
        next: (data: Cancelacion) => {
          this.cancelacion = data;
        },
        error: (err: any) => {
          this.cancelacion = null;

          if (err?.status !== 404) {
            this.error = err?.error?.message || 'Error al consultar cancelación';
          }
        }
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const idReservacion = Number(this.form.get('idReservacion')?.value);
    if (!idReservacion) {
      this.error = 'Debe seleccionar una reservación';
      return;
    }

    this.saving = true;
    this.error = '';
    this.success = '';

    this.cancelacionService.create(idReservacion)
      .pipe(
        finalize(() => {
          this.saving = false;
        })
      )
      .subscribe({
        next: (response: any) => {
          this.success = response?.message || 'Cancelación procesada correctamente';
          this.selectedReservacionId = idReservacion;
          this.loadCancelacionByReservacion(idReservacion);
          this.loadReservaciones();
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al procesar cancelación';
        }
      });
  }
}