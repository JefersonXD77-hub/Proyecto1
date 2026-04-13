export interface TipoProveedor {
  idTipoProveedor: number;
  nombre?: string;
}

export interface Proveedor {
  idProveedor?: number;
  nombre: string;
  tipoProveedor: TipoProveedor;
  paisOperacion: string;
  contacto: string;
  activo?: boolean;
}