import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { InventarioItem, RedBusqueda, ResumenExistencias } from '../models/inventario.model';

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
}
