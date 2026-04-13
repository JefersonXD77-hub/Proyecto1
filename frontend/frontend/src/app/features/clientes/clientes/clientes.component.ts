import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Cliente } from '../../../core/models/cliente.model';
import { ClienteService } from '../../../core/services/cliente.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrls: ['./clientes.component.css']
})
export class ClientesComponent implements OnInit {
  clientes: Cliente[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';
  editingId: number | null = null;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService
  ) {
    this.form = this.fb.group({
      dpiPasaporte: ['', Validators.required],
      nombreCompleto: ['', Validators.required],
      fechaNacimiento: ['', Validators.required],
      telefono: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      nacionalidad: ['', Validators.required],
      activo: [true]
    });
  }

  ngOnInit(): void {
    this.loadClientes();
  }

  loadClientes(): void {
  this.loading = true;
  this.error = '';

  this.clienteService.findAll()
    .pipe(
      finalize(() => {
        this.loading = false;
      })
    )
    .subscribe({
      next: (data) => {
        this.clientes = data;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al cargar clientes';
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

  const cliente: Cliente = this.form.getRawValue();

  const request$ = this.editingId
    ? this.clienteService.update(this.editingId, cliente)
    : this.clienteService.create(cliente);

  request$
    .pipe(
      finalize(() => {
        this.saving = false;
      })
    )
    .subscribe({
      next: () => {
        this.success = this.editingId
          ? 'Cliente actualizado correctamente'
          : 'Cliente creado correctamente';

        this.resetForm();
        this.loadClientes();
      },
      error: (err) => {
        this.error = err?.error?.message || (this.editingId
          ? 'Error al actualizar cliente'
          : 'Error al crear cliente');
      }
    });
}

  edit(cliente: Cliente): void {
    this.editingId = cliente.idCliente || null;
    this.success = '';
    this.error = '';

    this.form.patchValue({
      dpiPasaporte: cliente.dpiPasaporte,
      nombreCompleto: cliente.nombreCompleto,
      fechaNacimiento: cliente.fechaNacimiento,
      telefono: cliente.telefono,
      email: cliente.email,
      nacionalidad: cliente.nacionalidad,
      activo: cliente.activo ?? true
    });
  }

  deactivate(cliente: Cliente): void {
    if (!cliente.idCliente) return;

    const confirmado = confirm(`¿Desea desactivar a ${cliente.nombreCompleto}?`);
    if (!confirmado) return;

    this.error = '';
    this.success = '';

    this.clienteService.delete(cliente.idCliente).subscribe({
      next: () => {
        this.success = 'Cliente desactivado correctamente';
        this.loadClientes();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al desactivar cliente';
      }
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form.reset({
      dpiPasaporte: '',
      nombreCompleto: '',
      fechaNacimiento: '',
      telefono: '',
      email: '',
      nacionalidad: '',
      activo: true
    });
  }
}