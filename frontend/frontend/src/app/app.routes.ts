import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard.component';
import { ReservacionesComponent } from './features/reservaciones/reservaciones/reservaciones.component';
import { PagosComponent } from './features/pagos/pagos/pagos.component';
import { CancelacionesComponent } from './features/cancelaciones/cancelaciones/cancelaciones.component';
import { DestinosComponent } from './features/destinos/destinos/destinos.component';
import { ProveedoresComponent } from './features/proveedores/proveedores/proveedores.component';
import { PaquetesComponent } from './features/paquetes/paquetes/paquetes.component';
import { UsuariosComponent } from './features/usuarios/usuarios/usuarios.component';
import { CargaMasivaComponent } from './features/carga-masiva/carga-masiva/carga-masiva.component';
import { ReportesComponent } from './features/reportes/reportes/reportes.component';
import { ClientesComponent } from './features/clientes/clientes/clientes.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },

  {
    path: 'clientes',
    component: ClientesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'ATENCION_CLIENTE'] }
  },
  {
    path: 'reservaciones',
    component: ReservacionesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'ATENCION_CLIENTE'] }
  },
  {
    path: 'pagos',
    component: PagosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'ATENCION_CLIENTE'] }
  },
  {
    path: 'cancelaciones',
    component: CancelacionesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'ATENCION_CLIENTE'] }
  },

  {
    path: 'destinos',
    component: DestinosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'OPERACIONES'] }
  },
  {
    path: 'proveedores',
    component: ProveedoresComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'OPERACIONES'] }
  },
  {
    path: 'paquetes',
    component: PaquetesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'OPERACIONES'] }
  },

  {
    path: 'usuarios',
    component: UsuariosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'carga-masiva',
    component: CargaMasivaComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'reportes',
    component: ReportesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },

  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];