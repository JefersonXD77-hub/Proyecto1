export type TipoReporte =
  | 'ventas'
  | 'cancelaciones'
  | 'ganancias'
  | 'agente-mas-ventas'
  | 'agente-mas-ganancias'
  | 'paquete-mas-vendido'
  | 'paquete-menos-vendido'
  | 'ocupacion-destino';

export interface FiltrosReporte {
  fechaInicio?: string | null;
  fechaFin?: string | null;
}

export interface ReporteResponse<T = any> {
  status: string;
  fechaInicio?: string | null;
  fechaFin?: string | null;
  data: T;
}

export interface ReporteOption {
  value: TipoReporte;
  label: string;
}