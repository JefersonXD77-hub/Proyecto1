import { MetodoPago } from './metodo-pago.model';

export interface ReservacionResumenPago {
  numeroReservacion: string;
  costoTotal: number;
  totalPagado: number;
  saldoPendiente: number;
  estadoActual: string;
}

export interface Pago {
  idPago: number;
  monto: number;
  fechaPago: string;
  metodoPago: MetodoPago;
}

export interface PagoReservacionResponse {
  resumen: ReservacionResumenPago;
  data: Pago[];
}