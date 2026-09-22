import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config';
import { VerificacionCertificado } from '../models/certificado.model';

// RF-41: verificación pública (sin sesión) de un certificado de calidad escaneado por QR.
@Injectable({ providedIn: 'root' })
export class CertificadoPublicoService {

  constructor(private http: HttpClient) {}

  verificar(codigo: string): Observable<VerificacionCertificado> {
    return this.http.get<VerificacionCertificado>(`${API_BASE}/certificados/verificar/${encodeURIComponent(codigo)}`);
  }
}
