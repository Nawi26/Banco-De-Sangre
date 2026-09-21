export interface TurnoPersonal {
  id: number;
  usuarioId: number;
  usuarioNombreCompleto: string;
  fechaInicio: string;
  fechaFin: string;
  tipoTurno: string;
  observaciones: string | null;
}

export interface CrearTurnoRequest {
  usuarioId: number;
  fechaInicio: string;
  fechaFin: string;
  tipoTurno: string;
  observaciones: string;
}

export interface MantenimientoEquipo {
  id: number;
  nombreEquipo: string;
  tipoEquipo: string;
  tipoMantenimiento: string;
  fechaRealizado: string;
  fechaProximoVencimiento: string | null;
  responsableNombreCompleto: string | null;
  observaciones: string | null;
}

export interface RegistrarMantenimientoRequest {
  nombreEquipo: string;
  tipoEquipo: string;
  tipoMantenimiento: string;
  fechaRealizado: string;
  fechaProximoVencimiento: string | null;
  observaciones: string;
}

export interface ProtocoloClinico {
  id: number;
  nombre: string;
  version: string;
  contenidoUrl: string | null;
  notasCambio: string | null;
  vigente: boolean;
  publicadoPorNombreCompleto: string | null;
  fechaPublicacion: string;
}

export interface PublicarProtocoloRequest {
  nombre: string;
  version: string;
  contenidoUrl: string;
  notasCambio: string;
}

export interface RespaldoBaseDatos {
  id: number;
  fechaInicio: string;
  fechaFin: string | null;
  rutaArchivo: string | null;
  tamanioBytes: number | null;
  hashIntegridad: string | null;
  estado: string;
  detalle: string | null;
}

export interface VerificacionIntegridad {
  respaldoId: number;
  integro: boolean;
  mensaje: string;
}

export interface EventoDisponibilidad {
  tipo: string;
  fecha: string;
}

export interface Disponibilidad {
  enLineaDesde: string | null;
  segundosActivo: number;
  historial: EventoDisponibilidad[];
}
