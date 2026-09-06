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
  fechaSolicitud: string | null;
}

export interface CrearSolicitudRequest {
  pacienteId: number;
  tipoHemocomponente: string;
  unidadesSolicitadas: number;
  prioridad: string;
  indicacionClinica: string;
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

export interface CrearTransfusionRequest {
  solicitudId: number;
  codigoProductoIsbt: string;
  resultadoPruebaCruzada: string;
  reaccionAdversa: boolean;
  detallesReaccion: string;
}
