import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Reservacion } from '../../../core/models/reservacion.model';
import { MetodoPago } from '../../../core/models/metodo-pago.model';
import { Pago, ReservacionResumenPago } from '../../../core/models/pago.model';
import { ReservacionService } from '../../../core/services/reservacion.service';
import { MetodoPagoService } from '../../../core/services/metodo-pago.service';
import { PagoService } from '../../../core/services/pago.service';


@Component({
  selector: 'app-pagos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './pagos.component.html',
  styleUrls: ['./pagos.component.css']
})
export class PagosComponent implements OnInit {
  reservaciones: Reservacion[] = [];
  metodosPago: MetodoPago[] = [];
  pagos: Pago[] = [];
  resumen: ReservacionResumenPago | null = null;

  selectedReservacionId: number | null = null;

  loadingReservaciones = false;
  loadingMetodos = false;
  loadingPagos = false;
  saving = false;

  error = '';
  success = '';

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private reservacionService: ReservacionService,
    private metodoPagoService: MetodoPagoService,
    private pagoService: PagoService
  ) {
    this.form = this.fb.group({
      idReservacion: ['', Validators.required],
      idMetodoPago: ['', Validators.required],
      monto: ['', [Validators.required, Validators.min(0.01)]],
      fechaPago: ['']
    });
  }

  ngOnInit(): void {
    this.loadReservaciones();
    this.loadMetodosPago();
  }

  loadReservaciones(): void {
  this.loadingReservaciones = true;
  this.error = '';

  this.reservacionService.findAll()
    .pipe(finalize(() => (this.loadingReservaciones = false)))
    .subscribe({
      next: (data: Reservacion[]) => {
        this.reservaciones = data.filter(r => r.activo !== false);
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al cargar reservaciones';
      }
    });
}

  loadMetodosPago(): void {
  this.loadingMetodos = true;
  this.error = '';

  this.metodoPagoService.findAll()
    .pipe(finalize(() => (this.loadingMetodos = false)))
    .subscribe({
      next: (data: MetodoPago[]) => {
        this.metodosPago = data;
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al cargar métodos de pago';
      }
    });
}

  onReservacionChange(): void {
    const idReservacion = Number(this.form.get('idReservacion')?.value);
    this.selectedReservacionId = idReservacion || null;
    this.pagos = [];
    this.resumen = null;
    this.success = '';
    this.error = '';

    if (!this.selectedReservacionId) {
      return;
    }

    this.loadPagosByReservacion(this.selectedReservacionId);
  }

  loadPagosByReservacion(idReservacion: number): void {
  this.loadingPagos = true;
  this.error = '';

  this.pagoService.findByReservacion(idReservacion)
    .pipe(finalize(() => (this.loadingPagos = false)))
    .subscribe({
      next: (response: any) => {
        this.resumen = response.resumen;
        this.pagos = response.data;
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al cargar pagos';
      }
    });
}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.error = '';
    this.success = '';

    const raw = this.form.getRawValue();

    const body = {
      idReservacion: Number(raw.idReservacion),
      idMetodoPago: Number(raw.idMetodoPago),
      monto: Number(raw.monto),
      fechaPago: raw.fechaPago || undefined
    };

    this.pagoService.create(body)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (response) => {
          this.success = response?.message || 'Pago registrado correctamente';

          const idReservacion = Number(raw.idReservacion);
          this.selectedReservacionId = idReservacion;
          this.loadPagosByReservacion(idReservacion);

          this.form.patchValue({
            idMetodoPago: '',
            monto: '',
            fechaPago: ''
          });

          if (response?.pagoCompleto) {
            this.success += ' La reservación quedó pagada completamente.';
          }
        },
        error: (err) => {
          this.error = err?.error?.message || 'Error al registrar pago';
        }
      });
  }

  abrirComprobante(): void {
    if (!this.selectedReservacionId || !this.resumen) return;
    if (this.resumen.saldoPendiente > 0) return;

    window.open(this.pagoService.getComprobanteUrl(this.selectedReservacionId), '_blank');
  }
}