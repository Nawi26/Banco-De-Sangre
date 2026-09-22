import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { AuditoriaLog, PaginaSpring } from '../models/auditoria.model';

// RF-16/RF-17/RF-30: consulta de la bitácora de auditoría inmutable.
@Injectable({ providedIn: 'root' })
export class AuditoriaService {

  constructor(private http: HttpClient) {}

  listar(pagina = 0, tamano = 50): Observable<PaginaSpring<AuditoriaLog>> {
    return this.http.get<PaginaSpring<AuditoriaLog>>(`${API_BASE}/auditoria?pagina=${pagina}&tamano=${tamano}`);
  }
}
