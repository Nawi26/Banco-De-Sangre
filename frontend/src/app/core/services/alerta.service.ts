import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { AlertaStockCritico, AlertaVencimiento } from '../models/alerta.model';

// RF-14: alertas de proximidad de vencimiento y quiebre de stock crítico.
@Injectable({ providedIn: 'root' })
export class AlertaService {

  constructor(private http: HttpClient) {}

  unidadesPorVencer(): Observable<AlertaVencimiento[]> {
    return this.http.get<AlertaVencimiento[]>(`${API_BASE}/alertas/vencimiento`);
  }

  stockCritico(): Observable<AlertaStockCritico[]> {
    return this.http.get<AlertaStockCritico[]>(`${API_BASE}/alertas/stock-critico`);
  }
}
