export interface TicketSoporte {
  id: number;
  usuarioNombreCompleto: string;
  asunto: string;
  descripcion: string;
  prioridad: string;
  estado: string;
  respuesta: string | null;
  resueltoPorNombreCompleto: string | null;
  creadoEn: string;
  actualizadoEn: string;
}

export interface CrearTicketSoporteRequest {
  asunto: string;
  descripcion: string;
  prioridad: string;
}

export interface ResponderTicketSoporteRequest {
  estado: string;
  respuesta: string;
}
