import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import {
  CampanaDonacion, CitaDonacion, CrearCampanaRequest, CrearCitaRequest,
  CrearDonacionRequest, CrearDonanteRequest, CrearPacienteRequest, CrearReservaQuirurgicaRequest,
  CrearSolicitudRequest, CrearTransfusionRequest, ConfirmarDespachoRequest, Despacho, Donacion,
  Donante, EnviarMensajeRequest, EventoAdversoDonacion, EventoAdversoTransfusional, FraccionarDonacionRequest,
  Hemocomponente, ImportarOrdenHisSisRequest, IniciarDespachoRequest,
  Mensaje, Paciente, PruebaCompatibilidad, RegistrarEventoAdversoDonacionRequest,
  RegistrarEventoAdversoTransfusionalRequest, RegistrarPruebaCompatibilidadRequest, RegistrarTamizajeRequest,
  ReprogramarCitaRequest, ResolverDiscordanciaRequest, ReservaQuirurgica,
  Solicitud, TamizajeSerologico, Transfusion
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

  // RF-06: fracciona la bolsa de sangre total en los hemocomponentes indicados.
  fraccionarDonacion(donacionId: number, request: FraccionarDonacionRequest): Observable<Hemocomponente[]> {
    return this.http.post<Hemocomponente[]>(`${API_BASE}/donaciones/${donacionId}/fraccionamiento`, request);
  }

  listarHemocomponentesDonacion(donacionId: number): Observable<Hemocomponente[]> {
    return this.http.get<Hemocomponente[]>(`${API_BASE}/donaciones/${donacionId}/hemocomponentes`);
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

  // Tamizaje serológico de 7 marcadores, doble digitación ciega (RF-07/RF-08/RF-31)
  obtenerTamizaje(donacionId: number): Observable<TamizajeSerologico> {
    return this.http.get<TamizajeSerologico>(`${API_BASE}/donaciones/${donacionId}/tamizaje`);
  }

  primeraDigitacionTamizaje(donacionId: number, request: RegistrarTamizajeRequest): Observable<TamizajeSerologico> {
    return this.http.post<TamizajeSerologico>(`${API_BASE}/donaciones/${donacionId}/tamizaje/primera-digitacion`, request);
  }

  segundaDigitacionTamizaje(donacionId: number, request: RegistrarTamizajeRequest): Observable<TamizajeSerologico> {
    return this.http.post<TamizajeSerologico>(`${API_BASE}/donaciones/${donacionId}/tamizaje/segunda-digitacion`, request);
  }

  resolverDiscordanciaTamizaje(donacionId: number, request: ResolverDiscordanciaRequest): Observable<TamizajeSerologico> {
    return this.http.post<TamizajeSerologico>(`${API_BASE}/donaciones/${donacionId}/tamizaje/resolver-discordancia`, request);
  }

  // Campañas externas de captación de donantes (RF-23/RF-35)
  listarCampanas(): Observable<CampanaDonacion[]> {
    return this.http.get<CampanaDonacion[]>(`${API_BASE}/campanas`);
  }

  crearCampana(request: CrearCampanaRequest): Observable<CampanaDonacion> {
    return this.http.post<CampanaDonacion>(`${API_BASE}/campanas`, request);
  }

  // Citas de donación con recordatorio automático (RF-22)
  listarCitas(donanteId?: number): Observable<CitaDonacion[]> {
    const url = donanteId ? `${API_BASE}/citas-donacion?donanteId=${donanteId}` : `${API_BASE}/citas-donacion`;
    return this.http.get<CitaDonacion[]>(url);
  }

  citasProximas(horas = 48): Observable<CitaDonacion[]> {
    return this.http.get<CitaDonacion[]>(`${API_BASE}/citas-donacion/proximas?horas=${horas}`);
  }

  crearCita(request: CrearCitaRequest): Observable<CitaDonacion> {
    return this.http.post<CitaDonacion>(`${API_BASE}/citas-donacion`, request);
  }

  reprogramarCita(id: number, request: ReprogramarCitaRequest): Observable<CitaDonacion> {
    return this.http.patch<CitaDonacion>(`${API_BASE}/citas-donacion/${id}`, request);
  }

  // Integración HIS/SIS: importación automática de órdenes transfusionales (RF-32)
  importarOrdenHisSis(request: ImportarOrdenHisSisRequest): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${API_BASE}/his-sis/ordenes-transfusionales`, request);
  }

  // Mensajería interna por solicitud (RF-48)
  listarMensajes(solicitudId: number): Observable<Mensaje[]> {
    return this.http.get<Mensaje[]>(`${API_BASE}/solicitudes/${solicitudId}/mensajes`);
  }

  enviarMensaje(solicitudId: number, request: EnviarMensajeRequest): Observable<Mensaje> {
    return this.http.post<Mensaje>(`${API_BASE}/solicitudes/${solicitudId}/mensajes`, request);
  }
}
