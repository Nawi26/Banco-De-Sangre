import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import {
  CrearDonacionRequest, CrearDonanteRequest, CrearPacienteRequest, CrearReservaQuirurgicaRequest,
  CrearSolicitudRequest, CrearTransfusionRequest, ConfirmarDespachoRequest, Despacho, Donacion,
  Donante, EnviarMensajeRequest, EventoAdversoDonacion, EventoAdversoTransfusional, IniciarDespachoRequest,
  Mensaje, Paciente, PruebaCompatibilidad, RegistrarEventoAdversoDonacionRequest,
  RegistrarEventoAdversoTransfusionalRequest, RegistrarPruebaCompatibilidadRequest, ReservaQuirurgica,
  Solicitud, Transfusion
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

  // Pruebas cruzadas / RAI (RF-12)
  listarPruebasCruzadas(solicitudId: number): Observable<PruebaCompatibilidad[]> {
    return this.http.get<PruebaCompatibilidad[]>(`${API_BASE}/solicitudes/${solicitudId}/pruebas-cruzadas`);
  }

  registrarPruebaCruzada(solicitudId: number, request: RegistrarPruebaCompatibilidadRequest): Observable<PruebaCompatibilidad> {
    return this.http.post<PruebaCompatibilidad>(`${API_BASE}/solicitudes/${solicitudId}/pruebas-cruzadas`, request);
  }

  // Despacho con doble verificación electrónica (RF-13)
  listarDespachosPendientes(): Observable<Despacho[]> {
    return this.http.get<Despacho[]>(`${API_BASE}/despachos/pendientes`);
  }

  iniciarDespacho(request: IniciarDespachoRequest): Observable<Despacho> {
    return this.http.post<Despacho>(`${API_BASE}/despachos`, request);
  }

  confirmarDespacho(id: number, request: ConfirmarDespachoRequest): Observable<Despacho> {
    return this.http.post<Despacho>(`${API_BASE}/despachos/${id}/confirmar`, request);
  }

  // Reserva quirúrgica con liberación automática (RF-25)
  listarReservasQuirurgicas(): Observable<ReservaQuirurgica[]> {
    return this.http.get<ReservaQuirurgica[]>(`${API_BASE}/reservas-quirurgicas`);
  }

  crearReservaQuirurgica(request: CrearReservaQuirurgicaRequest): Observable<ReservaQuirurgica> {
    return this.http.post<ReservaQuirurgica>(`${API_BASE}/reservas-quirurgicas`, request);
  }

  liberarReservaQuirurgica(id: number): Observable<{ exito: boolean }> {
    return this.http.patch<{ exito: boolean }>(`${API_BASE}/reservas-quirurgicas/${id}/liberar`, {});
  }

  marcarReservaQuirurgicaUtilizada(id: number): Observable<{ exito: boolean }> {
    return this.http.patch<{ exito: boolean }>(`${API_BASE}/reservas-quirurgicas/${id}/utilizada`, {});
  }

  // Hemovigilancia: reacciones transfusionales (RF-15)
  listarEventosAdversosTransfusionales(): Observable<EventoAdversoTransfusional[]> {
    return this.http.get<EventoAdversoTransfusional[]>(`${API_BASE}/hemovigilancia/transfusiones`);
  }

  registrarEventoAdversoTransfusional(transfusionId: number, request: RegistrarEventoAdversoTransfusionalRequest): Observable<EventoAdversoTransfusional> {
    return this.http.post<EventoAdversoTransfusional>(`${API_BASE}/transfusiones/${transfusionId}/eventos-adversos`, request);
  }

  // Hemovigilancia: eventos adversos de la donación (RF-43)
  listarEventosAdversosDonacion(): Observable<EventoAdversoDonacion[]> {
    return this.http.get<EventoAdversoDonacion[]>(`${API_BASE}/hemovigilancia/donaciones`);
  }

  registrarEventoAdversoDonacion(donacionId: number, request: RegistrarEventoAdversoDonacionRequest): Observable<EventoAdversoDonacion> {
    return this.http.post<EventoAdversoDonacion>(`${API_BASE}/donaciones/${donacionId}/eventos-adversos`, request);
  }

  // Mensajería interna por solicitud (RF-48)
  listarMensajes(solicitudId: number): Observable<Mensaje[]> {
    return this.http.get<Mensaje[]>(`${API_BASE}/solicitudes/${solicitudId}/mensajes`);
  }

  enviarMensaje(solicitudId: number, request: EnviarMensajeRequest): Observable<Mensaje> {
    return this.http.post<Mensaje>(`${API_BASE}/solicitudes/${solicitudId}/mensajes`, request);
  }
}
