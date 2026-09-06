import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import {
  CrearDonacionRequest, CrearDonanteRequest, CrearPacienteRequest, CrearSolicitudRequest,
  CrearTransfusionRequest, Donacion, Donante, Paciente, Solicitud, Transfusion
} from '../models/clinico.model';

@Injectable({ providedIn: 'root' })
export class ClinicoService {

  constructor(private http: HttpClient) {}

  // Donantes
  listarDonantes(): Observable<Donante[]> {
    return this.http.get<Donante[]>(`${API_BASE}/donantes`);
  }

  crearDonante(request: CrearDonanteRequest): Observable<Donante> {
    return this.http.post<Donante>(`${API_BASE}/donantes`, request);
  }

  // Donaciones
  listarDonaciones(): Observable<Donacion[]> {
    return this.http.get<Donacion[]>(`${API_BASE}/donaciones`);
  }

  crearDonacion(request: CrearDonacionRequest): Observable<Donacion> {
    return this.http.post<Donacion>(`${API_BASE}/donaciones`, request);
  }

  // Pacientes
  listarPacientes(): Observable<Paciente[]> {
    return this.http.get<Paciente[]>(`${API_BASE}/pacientes`);
  }

  crearPaciente(request: CrearPacienteRequest): Observable<Paciente> {
    return this.http.post<Paciente>(`${API_BASE}/pacientes`, request);
  }

  // Solicitudes
  listarSolicitudes(): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(`${API_BASE}/solicitudes`);
  }

  crearSolicitud(request: CrearSolicitudRequest): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${API_BASE}/solicitudes`, request);
  }

  actualizarEstadoSolicitud(id: number, estado: string): Observable<{ exito: boolean }> {
    return this.http.patch<{ exito: boolean }>(`${API_BASE}/solicitudes/${id}/estado`, { estado });
  }

  eliminarSolicitud(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/solicitudes/${id}`);
  }

  // Transfusiones
  listarTransfusiones(): Observable<Transfusion[]> {
    return this.http.get<Transfusion[]>(`${API_BASE}/transfusiones`);
  }

  crearTransfusion(request: CrearTransfusionRequest): Observable<Transfusion> {
    return this.http.post<Transfusion>(`${API_BASE}/transfusiones`, request);
  }
}
