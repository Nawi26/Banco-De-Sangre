import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditoriaService } from '../../../core/services/auditoria.service';
import { AuditoriaLog } from '../../../core/models/auditoria.model';

// RF-16/RF-17/RF-30: bitácora de auditoría inmutable.
@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './auditoria.component.html'
})
export class AuditoriaComponent implements OnInit {
  auditoria: AuditoriaLog[] = [];
  pagina = 0;
  totalPaginas = 0;

  constructor(private auditoriaService: AuditoriaService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.auditoriaService.listar(this.pagina, 25).subscribe(resultado => {
      this.auditoria = resultado.content;
      this.totalPaginas = resultado.totalPages;
    });
  }

  irAPagina(delta: number): void {
    const nueva = this.pagina + delta;
    if (nueva < 0 || nueva >= this.totalPaginas) return;
    this.pagina = nueva;
    this.cargar();
  }
}
