import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';

import { Rol } from '../../../core/models/rol.model';
import { Usuario } from '../../../core/models/usuario.model';
import { UsuarioService } from '../../../core/services/usuario.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
  styleUrls: ['./usuarios.component.css']
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  roles: Rol[] = [];

  loading = false;
  loadingCatalogos = false;
  saving = false;
  error = '';
  success = '';
  editingId: number | null = null;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: [''],
      nombreCompleto: ['', Validators.required],
      correo: ['', Validators.email],
      idRol: ['', Validators.required],
      activo: [true]
    });
  }

  ngOnInit(): void {
    this.loadInicial();
  }

  loadInicial(): void {
    this.loading = true;
    this.loadingCatalogos = true;
    this.error = '';

    forkJoin({
      usuarios: this.usuarioService.findAll(),
      roles: this.usuarioService.findRoles()
    })
      .pipe(finalize(() => {
        this.loading = false;
        this.loadingCatalogos = false;
      }))
      .subscribe({
        next: ({ usuarios, roles }) => {
          this.usuarios = usuarios;
          this.roles = roles;
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al cargar usuarios';
        }
      });
  }

  loadUsuarios(): void {
    this.loading = true;
    this.error = '';

    this.usuarioService.findAll()
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe({
        next: (data: Usuario[]) => {
          this.usuarios = data;
        },
        error: (err: any) => {
          this.error = err?.error?.message || 'Error al cargar usuarios';
        }
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    if (!this.editingId && (!raw.password || raw.password.trim().length < 6)) {
      this.error = 'La password debe tener al menos 6 caracteres';
      return;
    }

    if (this.editingId && raw.password && raw.password.trim().length > 0 && raw.password.trim().length < 6) {
      this.error = 'La password debe tener al menos 6 caracteres';
      return;
    }

    this.saving = true;
    this.error = '';
    this.success = '';

    const body: any = {
      username: raw.username,
      nombreCompleto: raw.nombreCompleto,
      correo: raw.correo?.trim() ? raw.correo.trim() : null,
      idRol: Number(raw.idRol),
      activo: raw.activo
    };

    if (!this.editingId || (raw.password && raw.password.trim().length > 0)) {
      body.password = raw.password;
    }

    const request$ = this.editingId
      ? this.usuarioService.update(this.editingId, body)
      : this.usuarioService.create(body);

    request$
      .pipe(finalize(() => {
        this.saving = false;
      }))
      .subscribe({
        next: (response) => {
          this.success = response?.message || (
            this.editingId
              ? 'Usuario actualizado correctamente'
              : 'Usuario creado correctamente'
          );

          this.resetForm();
          this.loadUsuarios();
        },
        error: (err: any) => {
          this.error = err?.error?.message || (
            this.editingId
              ? 'Error al actualizar usuario'
              : 'Error al crear usuario'
          );
        }
      });
  }

  edit(usuario: Usuario): void {
    this.editingId = usuario.idUsuario || null;
    this.error = '';
    this.success = '';

    this.form.patchValue({
      username: usuario.username,
      password: '',
      nombreCompleto: usuario.nombreCompleto,
      correo: usuario.correo || '',
      idRol: usuario.rol.idRol,
      activo: usuario.activo ?? true
    });
  }

  deactivate(usuario: Usuario): void {
    if (!usuario.idUsuario) return;

    const confirmado = confirm(`¿Desea desactivar al usuario "${usuario.username}"?`);
    if (!confirmado) return;

    this.error = '';
    this.success = '';

    this.usuarioService.delete(usuario.idUsuario).subscribe({
      next: (response) => {
        this.success = response?.message || 'Usuario desactivado correctamente';
        this.loadUsuarios();
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Error al desactivar usuario';
      }
    });
  }

  resetForm(): void {
    this.editingId = null;

    this.form.reset({
      username: '',
      password: '',
      nombreCompleto: '',
      correo: '',
      idRol: '',
      activo: true
    });
  }
}