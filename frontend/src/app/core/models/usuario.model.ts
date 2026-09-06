export interface UsuarioSesion {
  id: number;
  nombreCompleto: string;
  rol: string;
}

export interface LoginResponse {
  exito: boolean;
  token: string;
  usuario: UsuarioSesion;
}

export interface UsuarioAdmin {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  email: string;
  colegiatura: string | null;
  estado: boolean;
  rolNombre: string;
}

export interface CrearUsuarioRequest {
  dni: string;
  nombres: string;
  apellidos: string;
  email: string;
  password: string;
  colegiatura: string;
  rolId: number;
}
