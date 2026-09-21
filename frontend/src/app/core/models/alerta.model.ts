export interface AlertaVencimiento {
  codigoProductoIsbt: string;
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  fechaVencimiento: string | null;
  estado: string;
}

export interface AlertaStockCritico {
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  unidadesDisponibles: number;
  umbralCritico: number;
}
