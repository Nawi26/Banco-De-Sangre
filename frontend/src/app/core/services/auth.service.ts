import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_BASE } from '../config';
import { LoginResponse, UsuarioSesion } from '../models/usuario.model';

const CLAVE_TOKEN = 'token';
const CLAVE_USUARIO = 'usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {

  // Signal reactivo con el usuario en sesión, para que la UI (sidebar, guards) reaccione a cambios
  usuario = signal<UsuarioSesion | null>(this.leerUsuarioAlmacenado());

  constructor(private http: HttpClient, private router: Router) {}

  login(identificador: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_BASE}/login`, { identificador, password }).pipe(
      tap(respuesta => {
        sessionStorage.setItem(CLAVE_TOKEN, respuesta.token);
        sessionStorage.setItem(CLAVE_USUARIO, JSON.stringify(respuesta.usuario));
        this.usuario.set(respuesta.usuario);
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(CLAVE_TOKEN);
    sessionStorage.removeItem(CLAVE_USUARIO);
    this.usuario.set(null);
    this.router.navigateByUrl('/login');
  }

  getToken(): string | null {
    return sessionStorage.getItem(CLAVE_TOKEN);
  }

  estaAutenticado(): boolean {
    return !!this.getToken() && !!this.usuario();
  }

  esAdministrador(): boolean {
    return this.usuario()?.rol === 'Administrador';
  }

  tieneRol(...roles: string[]): boolean {
    const rolActual = this.usuario()?.rol;
    return !!rolActual && roles.includes(rolActual);
  }

  esMedicoSolicitante(): boolean {
    return this.tieneRol('Médico Solicitante');
  }

  esPersonalBancoSangre(): boolean {
    return this.tieneRol('Tecnólogo Médico', 'Jefe de Banco de Sangre', 'Administrador');
  }

  rutaPanel(): string {
    return '/panel/dashboard';
  }

  private leerUsuarioAlmacenado(): UsuarioSesion | null {
    const crudo = sessionStorage.getItem(CLAVE_USUARIO);
    return crudo ? JSON.parse(crudo) : null;
  }
}
