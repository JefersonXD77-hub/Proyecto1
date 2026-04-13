import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';

import { Destino } from '../../../core/models/destino.model';
import { Proveedor } from '../../../core/models/proveedor.model';
import { PaqueteAltaDemanda, PaqueteTuristico } from '../../../core/models/paquete-turistico.model';
import { ResumenCostosPaquete, ServicioPaquete } from '../../../core/models/servicio-paquete.model';

import { DestinoService } from '../../../core/services/destino.service';
import { ProveedorService } from '../../../core/services/proveedor.service';
import { PaqueteService } from '../../../core/services/paquete.service';
import { ServicioPaqueteService } from '../../../core/services/servicio-paquete.service';

@Component({
  selector: 'app-paquetes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './paquetes.component.html',
  styleUrls: ['./paquetes.component.css']
})
export class PaquetesComponent implements OnInit {
  paquetes: PaqueteTuristico[] = [];
  destinos: Destino[] = [];
  proveedores: Proveedor[] = [];
  servicios: ServicioPaquete[] = [];
  altaDemanda: PaqueteAltaDemanda[] = [];
  resumenCostos: ResumenCostosPaquete | null = null;

  loading = false;
  loadingCatalogos = false;
  loadingServicios = false;
  loadingAltaDemanda = false;
  saving = false;
  savingServicio = false;

  error = '';
  success = '';
  errorServicio = '';
  successServicio = '';

  editingId: number | null = null;
  editingServicioId: number | null = null;
  selectedPaqueteId: number | null = null;

  form: FormGroup;
  servicioForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private destinoService: DestinoService,
    private proveedorService: ProveedorService,
    private paqueteService: PaqueteService,
    private servicioPaqueteService: ServicioPaqueteService
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      idDestino: ['', Validators.required],
      duracionDias: ['', [Validators.required, Validators.min(1)]],
      descripcion: ['', Validators.required],
      precioVenta: ['', [Validators.required, Validators.min(0.01)]],
      capacidadMaxima: ['', [Validators.required, Validators.min(1)]],
      activo: [true]
    });

    this.servicioForm = this.fb.group({
      idProveedor: ['', Validators.required],
      descripcion: ['', Validators.required],
      costo: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.loadCatalogos();
    this.loadPaquetes();
    this.loadAltaDemanda();
  }

  loadCatalogos(): void {
    this.loadingCatalogos = true;
    this.error = '';

    forkJoin({
      destinos: this.destinoService.findAll(),
      proveedores: this.proveedorService.findAll()
    })
      .pipe(finalize(() => {
        this.loadingCatalogos = false;
      }))
      .subscribe({
        next: ({ destinos, proveedores }) => {
          this.destinos = destinos.filter(d => d.activo !== false);
          this.proveedores = proveedores.filter(p => p.activo !== false);
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al cargar catálogos';
        }
      });
  }

  loadPaquetes(): void {
    this.loading = true;
    this.error = '';

    this.paqueteService.findAll()
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe({
        next: (data: PaqueteTuristico[]) => {
          this.paquetes = data;
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al cargar paquetes';
        }
      });
  }

  loadAltaDemanda(): void {
    this.loadingAltaDemanda = true;

    this.paqueteService.findAltaDemanda()
      .pipe(finalize(() => {
        this.loadingAltaDemanda = false;
      }))
      .subscribe({
        next: (data: PaqueteAltaDemanda[]) => {
          this.altaDemanda = data;
        },
        error: () => {
          this.altaDemanda = [];
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
      nombre: raw.nombre,
      idDestino: Number(raw.idDestino),
      duracionDias: Number(raw.duracionDias),
      descripcion: raw.descripcion,
      precioVenta: Number(raw.precioVenta),
      capacidadMaxima: Number(raw.capacidadMaxima),
      activo: raw.activo
    };

    const request$ = this.editingId
      ? this.paqueteService.update(this.editingId, body)
      : this.paqueteService.create(body);

    request$
      .pipe(finalize(() => {
        this.saving = false;
      }))
      .subscribe({
        next: (response) => {
          this.success = response?.message || (
            this.editingId
              ? 'Paquete actualizado correctamente'
              : 'Paquete creado correctamente'
          );

          this.resetForm();
          this.loadPaquetes();
          this.loadAltaDemanda();
        },
        error: (err: any) => {
          this.error = err?.error?.message || (
            this.editingId
              ? 'Error al actualizar paquete'
              : 'Error al crear paquete'
          );
        }
      });
  }

  edit(paquete: PaqueteTuristico): void {
    this.editingId = paquete.idPaquete || null;
    this.error = '';
    this.success = '';

    this.form.patchValue({
      nombre: paquete.nombre,
      idDestino: paquete.destino?.idDestino ?? '',
      duracionDias: paquete.duracionDias,
      descripcion: paquete.descripcion,
      precioVenta: paquete.precioVenta,
      capacidadMaxima: paquete.capacidadMaxima,
      activo: paquete.activo ?? true
    });
  }

  deactivate(paquete: PaqueteTuristico): void {
    if (!paquete.idPaquete) return;

    const confirmado = confirm(`¿Desea desactivar el paquete "${paquete.nombre}"?`);
    if (!confirmado) return;

    this.error = '';
    this.success = '';

    this.paqueteService.delete(paquete.idPaquete).subscribe({
      next: (response) => {
        this.success = response?.message || 'Paquete desactivado correctamente';

        if (this.selectedPaqueteId === paquete.idPaquete) {
          this.clearServiciosPanel();
        }

        this.loadPaquetes();
        this.loadAltaDemanda();
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al desactivar paquete';
      }
    });
  }

  seleccionarPaquete(paquete: PaqueteTuristico): void {
    this.selectedPaqueteId = paquete.idPaquete || null;
    this.resetServicioForm();
    this.errorServicio = '';
    this.successServicio = '';

    if (!this.selectedPaqueteId) {
      this.servicios = [];
      this.resumenCostos = null;
      return;
    }

    this.loadServiciosPanel(this.selectedPaqueteId);
  }

  loadServiciosPanel(idPaquete: number): void {
    this.loadingServicios = true;
    this.errorServicio = '';

    forkJoin({
      servicios: this.servicioPaqueteService.findByPaquete(idPaquete),
      resumen: this.servicioPaqueteService.getResumenCostos(idPaquete)
    })
      .pipe(finalize(() => {
        this.loadingServicios = false;
      }))
      .subscribe({
        next: ({ servicios, resumen }) => {
          this.servicios = servicios;
          this.resumenCostos = resumen;
        },
        error: (err: any) => {
          this.errorServicio = err?.error?.message || 'Error al cargar servicios del paquete';
          this.servicios = [];
          this.resumenCostos = null;
        }
      });
  }

  submitServicio(): void {
    if (!this.selectedPaqueteId) {
      this.errorServicio = 'Debe seleccionar un paquete';
      return;
    }

    if (this.servicioForm.invalid) {
      this.servicioForm.markAllAsTouched();
      return;
    }

    this.savingServicio = true;
    this.errorServicio = '';
    this.successServicio = '';

    const raw = this.servicioForm.getRawValue();

    const body = {
      idPaquete: this.selectedPaqueteId,
      idProveedor: Number(raw.idProveedor),
      descripcion: raw.descripcion,
      costo: Number(raw.costo)
    };

    const request$ = this.editingServicioId
      ? this.servicioPaqueteService.update(this.editingServicioId, body)
      : this.servicioPaqueteService.create(body);

    request$
      .pipe(finalize(() => {
        this.savingServicio = false;
      }))
      .subscribe({
        next: (response) => {
          this.successServicio = response?.message || (
            this.editingServicioId
              ? 'Servicio actualizado correctamente'
              : 'Servicio agregado correctamente'
          );

          this.resetServicioForm();

          if (this.selectedPaqueteId) {
            this.loadServiciosPanel(this.selectedPaqueteId);
          }
        },
        error: (err: any) => {
          this.errorServicio = err?.error?.message || (
            this.editingServicioId
              ? 'Error al actualizar servicio'
              : 'Error al agregar servicio'
          );
        }
      });
  }

  editServicio(servicio: ServicioPaquete): void {
    this.editingServicioId = servicio.idServicioPaquete || null;
    this.errorServicio = '';
    this.successServicio = '';

    this.servicioForm.patchValue({
      idProveedor: servicio.proveedor?.idProveedor ?? '',
      descripcion: servicio.descripcion,
      costo: servicio.costo
    });
  }

  deleteServicio(servicio: ServicioPaquete): void {
    if (!servicio.idServicioPaquete) return;

    const confirmado = confirm(`¿Desea eliminar el servicio "${servicio.descripcion}"?`);
    if (!confirmado) return;

    this.errorServicio = '';
    this.successServicio = '';

    this.servicioPaqueteService.delete(servicio.idServicioPaquete).subscribe({
      next: (response) => {
        this.successServicio = response?.message || 'Servicio eliminado correctamente';

        if (this.editingServicioId === servicio.idServicioPaquete) {
          this.resetServicioForm();
        }

        if (this.selectedPaqueteId) {
          this.loadServiciosPanel(this.selectedPaqueteId);
        }
      },
      error: (err: any) => {
        this.errorServicio = err?.error?.message || 'Error al eliminar servicio';
      }
    });
  }

  resetForm(): void {
    this.editingId = null;

    this.form.reset({
      nombre: '',
      idDestino: '',
      duracionDias: '',
      descripcion: '',
      precioVenta: '',
      capacidadMaxima: '',
      activo: true
    });
  }

  resetServicioForm(): void {
    this.editingServicioId = null;

    this.servicioForm.reset({
      idProveedor: '',
      descripcion: '',
      costo: ''
    });
  }

  clearServiciosPanel(): void {
    this.selectedPaqueteId = null;
    this.servicios = [];
    this.resumenCostos = null;
    this.resetServicioForm();
    this.errorServicio = '';
    this.successServicio = '';
  }

  get selectedPaquete(): PaqueteTuristico | null {
    return this.paquetes.find(p => p.idPaquete === this.selectedPaqueteId) || null;
  }
}