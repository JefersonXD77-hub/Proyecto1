export interface Cliente {
  idCliente?: number;
  dpiPasaporte: string;
  nombreCompleto: string;
  fechaNacimiento: string;
  telefono: string;
  email: string;
  nacionalidad: string;
  activo?: boolean;
}