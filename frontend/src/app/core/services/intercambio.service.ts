import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { CrearIntercambioRequest, Hospital, Intercambio } from '../models/intercambio.model';

@Injectable({ providedIn: 'root' })
export class IntercambioService {

  constructor(private http: HttpClient) {}

  listarHospitales(): Observable<Hospital[]> {
    return this.http.get<Hospital[]>(`${API_BASE}/hospitales`);
  }

  listarIntercambios(): Observable<Intercambio[]> {
    return this.http.get<Intercambio[]>(`${API_BASE}/intercambios`);
  }

  crearIntercambio(request: CrearIntercambioRequest): Observable<{ exito: boolean; id: number }> {
    return this.http.post<{ exito: boolean; id: number }>(`${API_BASE}/intercambios`, request);
  }

  actualizarEstado(id: number, estado: string, temperaturaCadenaFrio?: number): Observable<{ exito: boolean }> {
    return this.http.patch<{ exito: boolean }>(`${API_BASE}/intercambios/${id}/estado`, { estado, temperaturaCadenaFrio });
  }
}
