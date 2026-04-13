import { PaqueteTuristico } from './paquete-turistico.model';
import { Proveedor } from './proveedor.model';

export interface ServicioPaquete {
  idServicioPaquete?: number;
  paquete: PaqueteTuristico;
  proveedor: Proveedor;
  descripcion: string;
  costo: number;
}

export interface ResumenCostosPaquete {
  idPaquete: number;
  nombrePaquete: string;
  precioVenta: number;
  costoTotal: number;
  gananciaBruta: number;
}