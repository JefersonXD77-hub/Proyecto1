import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { Cliente } from '../../../core/models/cliente.model';
import { PaqueteTuristico } from '../../../core/models/paquete-turistico.model';
import { Reservacion } from '../../../core/models/reservacion.model';
import { ClienteService } from '../../../core/services/cliente.service';
import { PaqueteService } from '../../../core/services/paquete.service';
import { ReservacionService } from '../../../core/services/reservacion.service';


@Component({
  selector: 'app-reservaciones',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reservaciones.component.html',
  styleUrls: ['./reservaciones.component.css']
})
export class ReservacionesComponent implements OnInit {
  reservaciones: Reservacion[] = [];
  clientes: Cliente[] = [];
  paquetes: PaqueteTuristico[] = [];

  loadingReservaciones = false;
  loadingCatalogos = false;
  saving = false;
  error = '';
  success = '';

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    private paqueteService: PaqueteService,
    private reservacionService: ReservacionService
  ) {
    this.form = this.fb.group({
      idPaquete: ['', Validators.required],
      fechaViaje: ['', Validators.required],
      pasajeros: [[], Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCatalogos();
    this.loadReservaciones();
  }

  loadCatalogos(): void {
  this.loadingCatalogos = true;
  this.error = '';

  forkJoin({
    clientes: this.clienteService.findAll(),
    paquetes: this.paqueteService.findAll()
  })
    .pipe(
      finalize(() => {
        this.loadingCatalogos = false;
      })
    )
    .subscribe({
      next: ({ clientes, paquetes }) => {
        this.clientes = clientes.filter(c => c.activo !== false);
        this.paquetes = paquetes.filter(p => p.activo !== false);
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al cargar catálogos';
      }
    });
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
      next: (data) => {
        this.reservaciones = data;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al cargar reservaciones';
      }
    });
}

  onPasajerosChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const values = Array.from(select.selectedOptions).map(option => Number(option.value));
    this.form.patchValue({ pasajeros: values });
  }

  submit(): void {
  if (this.form.invalid) {
    this.form.markAllAsTouched();
    return;
  }

  this.saving = true;
  this.error = '';
  this.success = '';

  const body = this.form.getRawValue();

  this.reservacionService.create(body)
    .pipe(
      finalize(() => {
        this.saving = false;
      })
    )
    .subscribe({
      next: (response: any) => {
        this.success = response?.message || 'Reservación creada correctamente';
        this.resetForm();
        this.loadReservaciones();
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al crear reservación';
      }
    });
}

  resetForm(): void {
  this.form.reset({
    idPaquete: '',
    fechaViaje: '',
    pasajeros: []
  });

  const select = document.querySelector('select[multiple]') as HTMLSelectElement | null;
  if (select) {
    Array.from(select.options).forEach(option => option.selected = false);
  }
}

  get selectedPaquete(): PaqueteTuristico | undefined {
    const idPaquete = Number(this.form.get('idPaquete')?.value);
    return this.paquetes.find(p => p.idPaquete === idPaquete);
  }

  get selectedPasajerosCount(): number {
    const pasajeros = this.form.get('pasajeros')?.value as number[] | null;
    return pasajeros?.length ?? 0;
  }

  get costoEstimado(): number {
    const paquete = this.selectedPaquete;
    if (!paquete) return 0;
    return paquete.precioVenta * this.selectedPasajerosCount;
  }
}