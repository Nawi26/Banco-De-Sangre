import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import {
  CamaraAlmacenamiento, CrearCamaraRequest, InventarioItem, IsbtEtiqueta, RedBusqueda,
  RegistroTemperatura, ResumenExistencias
} from '../models/inventario.model';

@Injectable({ providedIn: 'root' })
export class InventarioService {

  constructor(private http: HttpClient) {}

  listarInventario(): Observable<InventarioItem[]> {
    return this.http.get<InventarioItem[]>(`${API_BASE}/inventario`);
  }

  resumenExistencias(): Observable<ResumenExistencias[]> {
    return this.http.get<ResumenExistencias[]>(`${API_BASE}/inventario/resumen`);
  }

  buscarEnRed(codigo: string): Observable<RedBusqueda> {
    return this.http.get<RedBusqueda>(`${API_BASE}/red-interhospitalaria/${encodeURIComponent(codigo)}`);
  }

  // RF-05: resuelve la etiqueta ISBT 128 a partir del código de producto escaneado/digitado.
  leerEtiqueta(codigo: string): Observable<IsbtEtiqueta> {
    return this.http.post<IsbtEtiqueta>(`${API_BASE}/hemocomponentes/leer`, { codigo });
  }

  // RF-09/RF-24: cámaras de refrigeración/congelación y su monitoreo de temperatura.
  listarCamaras(): Observable<CamaraAlmacenamiento[]> {
    return this.http.get<CamaraAlmacenamiento[]>(`${API_BASE}/camaras`);
  }

  crearCamara(request: CrearCamaraRequest): Observable<CamaraAlmacenamiento> {
    return this.http.post<CamaraAlmacenamiento>(`${API_BASE}/camaras`, request);
  }

  registrarTemperatura(camaraId: number, temperatura: number): Observable<RegistroTemperatura> {
    return this.http.post<RegistroTemperatura>(`${API_BASE}/camaras/${camaraId}/temperaturas`, { temperatura });
  }

  listarTemperaturas(camaraId: number): Observable<RegistroTemperatura[]> {
    return this.http.get<RegistroTemperatura[]>(`${API_BASE}/camaras/${camaraId}/temperaturas`);
  }
}
