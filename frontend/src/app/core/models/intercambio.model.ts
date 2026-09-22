export interface Hospital {
  id: number;
  codigoRenipress: string;
  nombreEstablecimiento: string;
  categoriaIpress: string;
  tipoBancoSangre: string;
}

export interface Intercambio {
  id: number;
  estado: string;
  temperaturaCadenaFrio: number | null;
  responsableTransporte: string | null;
  fechaSolicitud: string | null;
  fechaRespuesta: string | null;
  codigoProductoIsbt: string;
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  hospitalSolicitante: string;
  hospitalProveedor: string;
}

export interface CrearIntercambioRequest {
  ipressSolicitanteId: number;
  ipressProveedoraId: number;
  codigoProductoIsbt: string;
  responsableTransporte: string;
}

// RF-45: convenios y contratos interinstitucionales vigentes.
export interface ConvenioInterinstitucional {
  id: number;
  ipressId: number;
  ipressNombre: string;
  numeroConvenio: string;
  objeto: string;
  fechaInicio: string;
  fechaFin: string | null;
  estado: string;
}

export interface CrearConvenioRequest {
  ipressId: number;
  numeroConvenio: string;
  objeto: string;
  fechaInicio: string;
  fechaFin: string | null;
}

export const ESTADOS_INTERCAMBIO = [
  { clave: 'PENDIENTE', etiqueta: 'Pendiente', color: 'secondary' },
  { clave: 'ACEPTADO', etiqueta: 'Aceptado', color: 'info' },
  { clave: 'EN_TRANSITO', etiqueta: 'En Tránsito', color: 'warning' },
  { clave: 'ENTREGADO', etiqueta: 'Entregado', color: 'success' },
  { clave: 'DEVUELTO', etiqueta: 'Devuelto', color: 'dark' },
  { clave: 'RECHAZADO', etiqueta: 'Rechazado', color: 'danger' }
] as const;

export const SIGUIENTES_ESTADOS: Record<string, { estado: string; etiqueta: string }[]> = {
  PENDIENTE: [{ estado: 'ACEPTADO', etiqueta: 'Aceptar' }, { estado: 'RECHAZADO', etiqueta: 'Rechazar' }],
  ACEPTADO: [{ estado: 'EN_TRANSITO', etiqueta: 'Iniciar Transporte' }],
  EN_TRANSITO: [{ estado: 'ENTREGADO', etiqueta: 'Marcar Entregado' }],
  ENTREGADO: [],
  DEVUELTO: [],
  RECHAZADO: []
};
