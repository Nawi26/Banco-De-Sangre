import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import {
  CrearTurnoRequest, Disponibilidad, MantenimientoEquipo, ProtocoloClinico, PublicarProtocoloRequest,
  RegistrarMantenimientoRequest, RespaldoBaseDatos, TurnoPersonal, VerificacionIntegridad
} from '../models/administracion.model';

// RF-34/RF-39/RF-42/RF-47/RF-49: administración y operación del sistema.
@Injectable({ providedIn: 'root' })
export class AdministracionService {

  constructor(private http: HttpClient) {}

  listarTurnos(): Observable<TurnoPersonal[]> {
    return this.http.get<TurnoPersonal[]>(`${API_BASE}/turnos`);
  }

  programarTurno(request: CrearTurnoRequest): Observable<TurnoPersonal> {
    return this.http.post<TurnoPersonal>(`${API_BASE}/turnos`, request);
  }

  eliminarTurno(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/turnos/${id}`);
  }

  listarMantenimientos(): Observable<MantenimientoEquipo[]> {
    return this.http.get<MantenimientoEquipo[]>(`${API_BASE}/mantenimientos`);
  }

  registrarMantenimiento(request: RegistrarMantenimientoRequest): Observable<MantenimientoEquipo> {
    return this.http.post<MantenimientoEquipo>(`${API_BASE}/mantenimientos`, request);
  }

  listarProtocolos(): Observable<ProtocoloClinico[]> {
    return this.http.get<ProtocoloClinico[]>(`${API_BASE}/protocolos`);
  }

  publicarProtocolo(request: PublicarProtocoloRequest): Observable<ProtocoloClinico> {
    return this.http.post<ProtocoloClinico>(`${API_BASE}/protocolos`, request);
  }

  listarRespaldos(): Observable<RespaldoBaseDatos[]> {
    return this.http.get<RespaldoBaseDatos[]>(`${API_BASE}/respaldos`);
  }

  ejecutarRespaldo(): Observable<RespaldoBaseDatos> {
    return this.http.post<RespaldoBaseDatos>(`${API_BASE}/respaldos/ejecutar`, {});
  }

  verificarIntegridad(id: number): Observable<VerificacionIntegridad> {
    return this.http.get<VerificacionIntegridad>(`${API_BASE}/respaldos/${id}/verificar`);
  }

  obtenerDisponibilidad(): Observable<Disponibilidad> {
    return this.http.get<Disponibilidad>(`${API_BASE}/disponibilidad`);
  }
}
