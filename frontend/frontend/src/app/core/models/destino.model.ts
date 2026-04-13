export interface Destino {
  idDestino?: number;
  nombre: string;
  pais: string;
  descripcion: string;
  climaEpoca: string;
  urlImagen: string;
  activo?: boolean;
}