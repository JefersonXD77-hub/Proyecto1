import { Rol } from './rol.model';

export interface Usuario {
  idUsuario?: number;
  username: string;
  passwordHash?: string;
  nombreCompleto: string;
  correo?: string | null;
  rol: Rol;
  activo?: boolean;
  fechaCreacion?: string;
}