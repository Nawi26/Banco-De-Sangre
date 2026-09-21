export interface IndicadoresKpi {
  rotacionInventario: number;
  tasaMermaCaducidadPorcentaje: number;
  tiempoRespuestaPromedioMinutos: number | null;
  unidadesTransfundidasEnPeriodo: number;
  unidadesFraccionadasEnPeriodo: number;
  inventarioActivoActual: number;
}

export interface IndicadorCumplimiento {
  codigo: string;
  nombre: string;
  valorPorcentual: number;
  descripcion: string;
}

export interface ReporteOperacional {
  desde: string;
  hasta: string;
  donacionesRegistradas: number;
  transfusionesRealizadas: number;
  descartesPorMotivo: Record<string, number>;
  eventosAdversosTransfusionales: number;
  eventosAdversosDonacion: number;
  solicitudesPorEstado: Record<string, number>;
  certificadosEmitidos: number;
}

export interface ReporteNormativo {
  id: number;
  periodoDesde: string;
  periodoHasta: string;
  fechaGeneracion: string;
  donacionesRegistradas: number;
  transfusionesRealizadas: number;
  descartesTotal: number;
  eventosAdversosTotal: number;
  certificadosEmitidos: number;
}
