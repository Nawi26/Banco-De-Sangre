export interface AuditoriaLog {
  id: number;
  usuarioId: number | null;
  usuarioIdentificador: string | null;
  rol: string | null;
  metodoHttp: string;
  endpoint: string;
  accion: string | null;
  ipOrigen: string | null;
  resultadoHttp: number | null;
  exitoso: boolean;
  detalle: string | null;
  creadoEn: string;
}

// Forma estándar de una página de Spring Data (org.springframework.data.domain.Page).
export interface PaginaSpring<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
