import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { CrearUsuarioRequest, UsuarioAdmin } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  constructor(private http: HttpClient) {}

  listarMedicos(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(`${API_BASE}/usuarios`);
  }

  crearMedico(request: CrearUsuarioRequest): Observable<{ exito: boolean; usuario: UsuarioAdmin }> {
    return this.http.post<{ exito: boolean; usuario: UsuarioAdmin }>(`${API_BASE}/usuarios`, request);
  }
}
