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

// RF-05: contenido normalizado de la etiqueta ISBT 128 de una unidad.
export interface IsbtEtiqueta {
  codigoLineal: string;
  payload2D: string;
  dinMatriz: string | null;
  codigoProducto: string;
  grupoAboRh: string;
  fechaVencimiento: string | null;
}

// RF-09/RF-24: cámaras de refrigeración/congelación.
export interface CamaraAlmacenamiento {
  id: number;
  nombre: string;
  tipo: string;
  ubicacion: string | null;
  temperaturaMinima: number;
  temperaturaMaxima: number;
  activa: boolean;
}

export interface CrearCamaraRequest {
  nombre: string;
  tipo: string;
  ubicacion: string;
  temperaturaMinima: number;
  temperaturaMaxima: number;
}

export interface RegistroTemperatura {
  id: number;
  camaraId: number;
  camaraNombre: string;
  temperatura: number;
  dentroDeRango: boolean;
  registradoEn: string;
}

// RF-41: certificado de calidad emitido con código de verificación pública (QR).
export interface CertificadoCalidad {
  hemocomponenteId: number;
  codigoProductoIsbt: string;
  numeroCertificado: string;
  codigoVerificacion: string;
  emitidoEn: string;
}
