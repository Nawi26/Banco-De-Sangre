// RF-41: respuesta pública (sin autenticación) al escanear el QR de un certificado de calidad.
export interface VerificacionCertificado {
  valido: boolean;
  din: string | null;
  codigoProductoIsbt: string | null;
  tipoHemocomponente: string | null;
  grupoAboRh: string | null;
  fechaVencimiento: string | null;
  estadoActual: string | null;
  emitidoEn: string | null;
}
