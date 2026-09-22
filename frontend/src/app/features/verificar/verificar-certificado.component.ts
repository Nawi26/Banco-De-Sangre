import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { CertificadoPublicoService } from '../../core/services/certificado-publico.service';
import { VerificacionCertificado } from '../../core/models/certificado.model';

// RF-41: página pública (sin sesión) que resuelve al escanear el QR de un certificado de calidad.
@Component({
  selector: 'app-verificar-certificado',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './verificar-certificado.component.html'
})
export class VerificarCertificadoComponent implements OnInit {
  codigo = '';
  resultado: VerificacionCertificado | null = null;
  cargando = true;

  constructor(private route: ActivatedRoute, private certificadoService: CertificadoPublicoService) {}

  ngOnInit(): void {
    this.codigo = this.route.snapshot.paramMap.get('codigo') ?? '';
    if (!this.codigo) {
      this.cargando = false;
      return;
    }
    this.certificadoService.verificar(this.codigo).subscribe({
      next: (datos) => {
        this.resultado = datos;
        this.cargando = false;
      },
      error: () => {
        this.resultado = null;
        this.cargando = false;
      }
    });
  }
}
