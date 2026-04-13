import { Destino } from './destino.model';

export interface DestinoBasico {
  idDestino: number;
  nombre: string;
}

export interface PaqueteTuristico {
  idPaquete?: number;
  nombre: string;
  duracionDias: number;
  descripcion: string;
  precioVenta: number;
  capacidadMaxima: number;
  activo?: boolean;
  destino?: Destino | DestinoBasico;
}

export interface PaqueteAltaDemanda {
  idPaquete: number;
  nombrePaquete: string;
  idDestino: number;
  nombreDestino: string;
  fechaViaje: string;
  capacidadMaxima: number;
  cuposOcupados: number;
  porcentajeOcupacion: number;
}