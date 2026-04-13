import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Proveedor } from '../../../core/models/proveedor.model';
import { ProveedorService } from '../../../core/services/proveedor.service';

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './proveedores.component.html',
  styleUrls: ['./proveedores.component.css']
})
export class ProveedoresComponent implements OnInit {
  proveedores: Proveedor[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';
  editingId: number | null = null;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private proveedorService: ProveedorService
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      idTipoProveedor: ['', Validators.required],
      paisOperacion: ['', Validators.required],
      contacto: ['', Validators.required],
      activo: [true]
    });
  }

  ngOnInit(): void {
    this.loadProveedores();
  }

  loadProveedores(): void {
    this.loading = true;
    this.error = '';

    this.proveedorService.findAll()
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe({
        next: (data: Proveedor[]) => {
          this.proveedores = data;
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al cargar proveedores';
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
      idTipoProveedor: Number(raw.idTipoProveedor),
      paisOperacion: raw.paisOperacion,
      contacto: raw.contacto,
      activo: raw.activo
    };

    const request$ = this.editingId
      ? this.proveedorService.update(this.editingId, body)
      : this.proveedorService.create(body);

    request$
      .pipe(finalize(() => {
        this.saving = false;
      }))
      .subscribe({
        next: (response) => {
          this.success = response?.message || (
            this.editingId
              ? 'Proveedor actualizado correctamente'
              : 'Proveedor creado correctamente'
          );

          this.resetForm();
          this.loadProveedores();
        },
        error: (err: any) => {
          this.error = err?.error?.message || (
            this.editingId
              ? 'Error al actualizar proveedor'
              : 'Error al crear proveedor'
          );
        }
      });
  }

  edit(proveedor: Proveedor): void {
    this.editingId = proveedor.idProveedor || null;
    this.error = '';
    this.success = '';

    this.form.patchValue({
      nombre: proveedor.nombre,
      idTipoProveedor: proveedor.tipoProveedor?.idTipoProveedor ?? '',
      paisOperacion: proveedor.paisOperacion,
      contacto: proveedor.contacto,
      activo: proveedor.activo ?? true
    });
  }

  deactivate(proveedor: Proveedor): void {
    if (!proveedor.idProveedor) return;

    const confirmado = confirm(`¿Desea desactivar el proveedor "${proveedor.nombre}"?`);
    if (!confirmado) return;

    this.error = '';
    this.success = '';

    this.proveedorService.delete(proveedor.idProveedor).subscribe({
      next: (response) => {
        this.success = response?.message || 'Proveedor desactivado correctamente';
        this.loadProveedores();
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al desactivar proveedor';
      }
    });
  }

  resetForm(): void {
    this.editingId = null;

    this.form.reset({
      nombre: '',
      idTipoProveedor: '',
      paisOperacion: '',
      contacto: '',
      activo: true
    });
  }

  getTipoProveedorLabel(proveedor: Proveedor): string {
    return proveedor.tipoProveedor?.nombre || `Tipo ${proveedor.tipoProveedor?.idTipoProveedor ?? ''}`;
  }
}