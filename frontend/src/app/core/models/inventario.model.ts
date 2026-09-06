export interface InventarioItem {
  codigoProductoIsbt: string;
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  fechaVencimiento: string | null;
  estado: string;
}

export interface ResumenExistencias {
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  unidades: number;
  volumenTotalMl: number;
}

export interface RedBusqueda {
  codigoProductoIsbt: string;
  grupoAbo: string;
  factorRh: string;
  estado: string;
  ubicacionFisica: string;
}
