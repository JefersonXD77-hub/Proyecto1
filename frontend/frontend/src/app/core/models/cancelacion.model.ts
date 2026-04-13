export interface UsuarioProcesoBasico {
  idUsuario: number;
  username?: string;
  nombreCompleto?: string;
}

export interface ReservacionBasicaCancelacion {
  idReservacion: number;
  numeroReservacion?: string;
}

export interface Cancelacion {
  idCancelacion: number;
  reservacion: ReservacionBasicaCancelacion;
  fechaCancelacion: string;
  diasAnticipacion: number;
  porcentajeReembolso: number;
  montoPagado: number;
  montoReembolsado: number;
  perdidaAgencia: number;
  usuarioProceso: UsuarioProcesoBasico;
}