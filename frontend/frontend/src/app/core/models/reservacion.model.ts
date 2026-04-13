import { Cliente } from './cliente.model';
import { PaqueteTuristico } from './paquete-turistico.model';

export interface UsuarioBasico {
  idUsuario: number;
  username?: string;
  nombreCompleto?: string;
}

export interface Reservacion {
  idReservacion?: number;
  numeroReservacion?: string;
  fechaCreacion?: string;
  fechaViaje: string;
  paquete: PaqueteTuristico;
  agente?: UsuarioBasico;
  cantidadPasajeros: number;
  costoTotal: number;
  estado?: string;
  activo?: boolean;
  pasajeros: Cliente[];
}