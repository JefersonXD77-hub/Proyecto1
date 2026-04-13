import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Destino } from '../../../core/models/destino.model';
import { DestinoService } from '../../../core/services/destino.service';

@Component({
  selector: 'app-destinos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './destinos.component.html',
  styleUrls: ['./destinos.component.css']
})
export class DestinosComponent implements OnInit {
  destinos: Destino[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';
  editingId: number | null = null;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private destinoService: DestinoService
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      pais: ['', Validators.required],
      descripcion: ['', Validators.required],
      climaEpoca: ['', Validators.required],
      urlImagen: ['', Validators.required],
      activo: [true]
    });
  }

  ngOnInit(): void {
    this.loadDestinos();
  }

  loadDestinos(): void {
    this.loading = true;
    this.error = '';

    this.destinoService.findAll()
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (data: Destino[]) => {
          this.destinos = data;
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al cargar destinos';
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

    const destino: Destino = this.form.getRawValue();

    const request$ = this.editingId
      ? this.destinoService.update(this.editingId, destino)
      : this.destinoService.create(destino);

    request$
      .pipe(
        finalize(() => {
          this.saving = false;
        })
      )
      .subscribe({
        next: (response) => {
          this.success = response?.message || (
            this.editingId
              ? 'Destino actualizado correctamente'
              : 'Destino creado correctamente'
          );

          this.resetForm();
          this.loadDestinos();
        },
        error: (err: any) => {
          this.error = err?.error?.message || (
            this.editingId
              ? 'Error al actualizar destino'
              : 'Error al crear destino'
          );
        }
      });
  }

  edit(destino: Destino): void {
    this.editingId = destino.idDestino || null;
    this.success = '';
    this.error = '';

    this.form.patchValue({
      nombre: destino.nombre,
      pais: destino.pais,
      descripcion: destino.descripcion,
      climaEpoca: destino.climaEpoca,
      urlImagen: destino.urlImagen,
      activo: destino.activo ?? true
    });
  }

  deactivate(destino: Destino): void {
    if (!destino.idDestino) return;

    const confirmado = confirm(`¿Desea desactivar el destino "${destino.nombre}"?`);
    if (!confirmado) return;

    this.error = '';
    this.success = '';

    this.destinoService.delete(destino.idDestino).subscribe({
      next: (response) => {
        this.success = response?.message || 'Destino desactivado correctamente';
        this.loadDestinos();
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al desactivar destino';
      }
    });
  }

  resetForm(): void {
  this.editingId = null;

  this.form.reset({
    nombre: '',
    pais: '',
    descripcion: '',
    climaEpoca: '',
    urlImagen: '',
    activo: true
  });
}
}