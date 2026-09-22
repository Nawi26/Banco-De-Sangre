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

// RF-06: hemocomponente producido al fraccionar una donación de sangre total.
export interface Hemocomponente {
  id: number;
  codigoProductoIsbt: string;
  dinMatriz: string | null;
  tipoHemocomponente: string;
  grupoAbo: string;
  factorRh: string;
  volumenMl: number | null;
  fechaVencimiento: string | null;
  ubicacionFisica: string | null;
  estado: string;
  camaraId: number | null;
  camaraNombre: string | null;
}

export interface FraccionarDonacionRequest {
  tiposHemocomponente: string[];
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

// RF-15: hemovigilancia de reacciones transfusionales.
export interface EventoAdversoTransfusional {
  id: number;
  transfusionId: number;
  pacienteNombreCompleto: string | null;
  codigoProductoIsbt: string | null;
  tipoReaccion: string;
  esInmediata: boolean;
  gravedad: string;
  descripcion: string;
  accionesTomadas: string | null;
  usuarioNombreCompleto: string | null;
  fechaDeteccion: string;
}

export interface RegistrarEventoAdversoTransfusionalRequest {
  tipoReaccion: string;
  esInmediata: boolean;
  gravedad: string;
  descripcion: string;
  accionesTomadas: string;
}

// RF-43: hemovigilancia de eventos adversos de la donación.
export interface EventoAdversoDonacion {
  id: number;
  donacionId: number;
  donanteNombreCompleto: string | null;
  tipoEvento: string;
  gravedad: string;
  descripcion: string;
  accionesTomadas: string | null;
  usuarioNombreCompleto: string | null;
  fechaDeteccion: string;
}

export interface RegistrarEventoAdversoDonacionRequest {
  tipoEvento: string;
  gravedad: string;
  descripcion: string;
  accionesTomadas: string;
}

// RF-07/RF-31: tamizaje serológico de 7 marcadores infecciosos, con doble digitación ciega.
export const MARCADORES_SEROLOGICOS = [
  { valor: 'VIH_1_2', etiqueta: 'VIH 1/2' },
  { valor: 'HEPATITIS_B_HBSAG', etiqueta: 'Hepatitis B (HBsAg)' },
  { valor: 'HEPATITIS_B_ANTI_CORE', etiqueta: 'Hepatitis B (Anti-Core)' },
  { valor: 'HEPATITIS_C', etiqueta: 'Hepatitis C' },
  { valor: 'SIFILIS', etiqueta: 'Sífilis' },
  { valor: 'CHAGAS', etiqueta: 'Chagas' },
  { valor: 'HTLV_1_2', etiqueta: 'HTLV 1/2' }
];

export interface ResultadoMarcadorInput {
  marcador: string;
  resultado: string;
}

export interface ResultadoMarcador {
  marcador: string;
  resultadoDigitacion1: string | null;
  resultadoDigitacion2: string | null;
  resultadoFinal: string | null;
  concordante: boolean | null;
}

export interface TamizajeSerologico {
  id: number;
  donacionId: number;
  origen: string;
  estado: string;
  resultadoGeneral: string | null;
  fechaPrimeraDigitacion: string | null;
  fechaSegundaDigitacion: string | null;
  resultados: ResultadoMarcador[];
}

export interface RegistrarTamizajeRequest {
  resultados: ResultadoMarcadorInput[];
  origen: string;
  claveConfirmacion: string;
}

export interface ResolverDiscordanciaRequest {
  resultadosDefinitivos: ResultadoMarcadorInput[];
  claveConfirmacion: string;
}

// RF-23/RF-35: campañas externas de captación de donantes.
export interface CampanaDonacion {
  id: number;
  nombre: string;
  institucion: string | null;
  tipo: string | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  metaUnidades: number | null;
  unidadesLogradas: number;
  estado: string;
}

export interface CrearCampanaRequest {
  nombre: string;
  institucion: string;
  tipo: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  metaUnidades: number | null;
}

// RF-22: programación, reprogramación y recordatorio de citas de donación.
export interface CitaDonacion {
  id: number;
  donanteId: number;
  donanteNombreCompleto: string;
  campanaId: number | null;
  fechaHora: string;
  estado: string;
  notas: string | null;
}

export interface CrearCitaRequest {
  donanteId: number;
  campanaId: number | null;
  fechaHora: string;
  notas: string;
}

export interface ReprogramarCitaRequest {
  fechaHora: string | null;
  estado: string;
  notas: string;
}

// RF-32: integración con el HIS/SIS hospitalario para importar órdenes transfusionales.
export interface ImportarOrdenHisSisRequest {
  codigoOrdenExterna: string;
  pacienteNumDoc: string;
  pacienteNombres: string;
  pacienteApellidos: string;
  medicoDni: string;
  tipoHemocomponente: string;
  unidadesSolicitadas: number;
  prioridad: string;
  indicacionClinica: string;
  diagnosticoCie10: string;
}

// RF-48: mensajería interna banco de sangre <-> servicio asistencial, por solicitud.
export interface Mensaje {
  id: number;
  solicitudId: number;
  remitenteNombreCompleto: string;
  remitenteRol: string;
  contenido: string;
  fechaEnvio: string;
}

export interface EnviarMensajeRequest {
  contenido: string;
}
