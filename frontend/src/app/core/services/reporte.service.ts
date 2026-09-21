import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { IndicadorCumplimiento, IndicadoresKpi, ReporteNormativo, ReporteOperacional } from '../models/reporte.model';

// RF-16/RF-26/RF-27/RF-36/RF-44/RF-50: reportes gerenciales y de cumplimiento.
@Injectable({ providedIn: 'root' })
export class ReporteService {

  constructor(private http: HttpClient) {}

  obtenerKpis(desde?: string, hasta?: string): Observable<IndicadoresKpi> {
    return this.http.get<IndicadoresKpi>(`${API_BASE}/reportes/kpis`, { params: this.parametros(desde, hasta) });
  }

  obtenerCumplimiento(): Observable<IndicadorCumplimiento[]> {
    return this.http.get<IndicadorCumplimiento[]>(`${API_BASE}/reportes/cumplimiento`);
  }

  obtenerOperacional(desde?: string, hasta?: string): Observable<ReporteOperacional> {
    return this.http.get<ReporteOperacional>(`${API_BASE}/reportes/operacional`, { params: this.parametros(desde, hasta) });
  }

  descargarOperacionalCsv(desde?: string, hasta?: string): Observable<Blob> {
    return this.http.get(`${API_BASE}/reportes/operacional/csv`, { params: this.parametros(desde, hasta), responseType: 'blob' });
  }

  descargarDatasetAnonimizadoCsv(): Observable<Blob> {
    return this.http.get(`${API_BASE}/reportes/dataset-anonimizado/csv`, { responseType: 'blob' });
  }

  listarHistorialNormativo(): Observable<ReporteNormativo[]> {
    return this.http.get<ReporteNormativo[]>(`${API_BASE}/reportes/normativos/historial`);
  }

  generarNormativoManual(desde: string, hasta: string): Observable<ReporteNormativo> {
    return this.http.post<ReporteNormativo>(`${API_BASE}/reportes/normativos/generar`, {}, { params: { desde, hasta } });
  }

  private parametros(desde?: string, hasta?: string): Record<string, string> {
    const params: Record<string, string> = {};
    if (desde) params['desde'] = desde;
    if (hasta) params['hasta'] = hasta;
    return params;
  }
}
