export interface Donante {
  id: number;
  tipoDoc: string;
  numDoc: string;
  nombres: string;
  apellidos: string;
  fechaNacimiento: string | null;
  sexo: string | null;
  grupoAbo: string;
  factorRh: string;
  estadoDiferido: boolean;
  esOcupacionRiesgo: boolean;
}

export interface CrearDonanteRequest {
  tipoDoc: string;
  numDoc: string;
  nombres: string;
  apellidos: string;
  fechaNacimiento: string | null;
  sexo: string;
  grupoAbo: string;
  factorRh: string;
}

export interface Donacion {
  id: number;
  dinIsbt128: string;
  donanteId: number;
  donanteNombreCompleto: string;
  fechaExtraccion: string;
  volumenMl: number | null;
  tipoDonacion: string;
  tamizajeAprobado: boolean;
}

export interface CrearDonacionRequest {
  donanteId: number;
  volumenMl: number | null;
  tipoDonacion: string;
}

export interface Paciente {
  id: number;
  tipoDoc: string;
  numDoc: string;
  nombres: string;
  apellidos: string;
  fechaNacimiento: string | null;
  sexo: string | null;
  grupoAbo: string;
  factorRh: string;
}

export interface CrearPacienteRequest {
  tipoDoc: string;
  numDoc: string;
  nombres: string;
  apellidos: string;
  fechaNacimiento: string | null;
  sexo: string;
  grupoAbo: string;
  factorRh: string;
}

export interface Solicitud {
  id: number;
  codigoSolicitud: string;
  pacienteId: number;
  pacienteNombreCompleto: string;
  medicoNombreCompleto: string;
  tipoHemocomponente: string;
  unidadesSolicitadas: number;
  prioridad: string;
  estado: string;
  indicacionClinica: string | null;
  diagnosticoCie10: string | null;
  fechaSolicitud: string | null;
}

export interface CrearSolicitudRequest {
  pacienteId: number;
  tipoHemocomponente: string;
  unidadesSolicitadas: number;
  prioridad: string;
  indicacionClinica: string;
  // RF-11: diagnóstico que sustenta la solicitud, codificado en CIE-10 (ej. "D50", "O99.0").
  diagnosticoCie10: string;
}

export interface Transfusion {
  id: number;
  codigoSolicitud: string;
  pacienteNombreCompleto: string;
  codigoProductoIsbt: string;
  grupoAbo: string;
  factorRh: string;
  tecnologoNombreCompleto: string | null;
  medicoNombreCompleto: string | null;
  resultadoPruebaCruzada: string | null;
  fechaTransfusion: string | null;
  reaccionAdversa: boolean;
  detallesReaccion: string | null;
}

// RF-13: el resultado de compatibilidad ya no se declara aquí, se toma de la prueba
// cruzada (RF-12) ya registrada para la unidad despachada.
export interface CrearTransfusionRequest {
  solicitudId: number;
  codigoProductoIsbt: string;
  reaccionAdversa: boolean;
  detallesReaccion: string;
}

// RF-12: prueba cruzada y RAI de una unidad candidata frente a una solicitud.
export interface PruebaCompatibilidad {
  id: number;
  solicitudId: number;
  codigoProductoIsbt: string;
  resultadoRai: string;
  resultadoPruebaCruzada: string;
  tecnologoNombreCompleto: string | null;
  fecha: string;
}

export interface RegistrarPruebaCompatibilidadRequest {
  codigoProductoIsbt: string;
  resultadoRai: string;
  resultadoPruebaCruzada: string;
}

// RF-13: doble verificación electrónica del despacho.
export interface Despacho {
  id: number;
  codigoSolicitud: string;
  codigoProductoIsbt: string;
  primeraVerificacionNombreCompleto: string;
  primeraVerificacionEn: string;
  segundaVerificacionNombreCompleto: string | null;
  segundaVerificacionEn: string | null;
  estado: string;
  fechaDespacho: string | null;
}

export interface IniciarDespachoRequest {
  solicitudId: number;
  codigoProductoIsbt: string;
  claveConfirmacion: string;
}

export interface ConfirmarDespachoRequest {
  claveConfirmacion: string;
}

// RF-25: reserva quirúrgica con liberación automática.
export interface ReservaQuirurgica {
  id: number;
  pacienteId: number;
  pacienteNombreCompleto: string;
  medicoSolicitanteNombreCompleto: string;
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  unidadesSolicitadas: number;
  fechaCirugiaProgramada: string;
  horasValidezPostCirugia: number;
  estado: string;
  creadoEn: string;
}

export interface CrearReservaQuirurgicaRequest {
  pacienteId: number;
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  unidadesSolicitadas: number;
  fechaCirugiaProgramada: string;
  horasValidezPostCirugia: number | null;
}
