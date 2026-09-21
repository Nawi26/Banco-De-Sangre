import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { CrearTicketSoporteRequest, ResponderTicketSoporteRequest, TicketSoporte } from '../models/soporte.model';

// RF-33: mesa de ayuda interna.
@Injectable({ providedIn: 'root' })
export class SoporteService {

  constructor(private http: HttpClient) {}

  listarPropios(): Observable<TicketSoporte[]> {
    return this.http.get<TicketSoporte[]>(`${API_BASE}/tickets-soporte/propios`);
  }

  listarTodos(): Observable<TicketSoporte[]> {
    return this.http.get<TicketSoporte[]>(`${API_BASE}/tickets-soporte`);
  }

  crear(request: CrearTicketSoporteRequest): Observable<TicketSoporte> {
    return this.http.post<TicketSoporte>(`${API_BASE}/tickets-soporte`, request);
  }

  responder(id: number, request: ResponderTicketSoporteRequest): Observable<TicketSoporte> {
    return this.http.patch<TicketSoporte>(`${API_BASE}/tickets-soporte/${id}`, request);
  }
}
