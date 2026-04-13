export interface ErrorCargaMasiva {
  linea: number;
  contenido: string;
  error: string;
}

export interface ResumenRegistrosInsertados {
  usuarios: number;
  destinos: number;
  proveedores: number;
  paquetes: number;
  serviciosPaquete: number;
  clientes: number;
  reservaciones: number;
  pagos: number;
}

export interface ResumenCargaMasiva {
  totalLineasProcesadas: number;
  lineasExitosas: number;
  lineasConError: number;
  registrosInsertados: ResumenRegistrosInsertados;
}

export interface RespuestaCargaMasiva {
  status: string;
  message?: string;
  resumen: ResumenCargaMasiva;
  errores: ErrorCargaMasiva[];
}