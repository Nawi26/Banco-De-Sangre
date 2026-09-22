import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../core/services/reporte.service';
import { IndicadorCumplimiento, IndicadoresKpi, ProyeccionDesabastecimiento, ReporteNormativo, ReporteOperacional } from '../../../core/models/reporte.model';

function haceDias(dias: number): string {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() - dias);
  return fecha.toISOString().slice(0, 16);
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html'
})
export class ReportesComponent implements OnInit {
  desde = haceDias(30);
  hasta = new Date().toISOString().slice(0, 16);

  kpis: IndicadoresKpi | null = null;
  cumplimiento: IndicadorCumplimiento[] = [];
  operacional: ReporteOperacional | null = null;
  historialNormativo: ReporteNormativo[] = [];
  proyecciones: ProyeccionDesabastecimiento[] = [];

  mensaje = '';
  exito = false;

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    this.actualizar();
    this.cargarCumplimiento();
    this.cargarHistorialNormativo();
    this.cargarProyecciones();
  }

  cargarProyecciones(): void {
    this.reporteService.obtenerProyeccionDesabastecimiento().subscribe(datos => this.proyecciones = datos);
  }

  proyeccionesConRiesgo(): ProyeccionDesabastecimiento[] {
    return this.proyecciones.filter(p => p.nivelRiesgo !== 'BAJO');
  }

  actualizar(): void {
    this.reporteService.obtenerKpis(this.desde, this.hasta).subscribe(datos => this.kpis = datos);
    this.reporteService.obtenerOperacional(this.desde, this.hasta).subscribe(datos => this.operacional = datos);
  }

  cargarCumplimiento(): void {
    this.reporteService.obtenerCumplimiento().subscribe(datos => this.cumplimiento = datos);
  }

  cargarHistorialNormativo(): void {
    this.reporteService.listarHistorialNormativo().subscribe(datos => this.historialNormativo = datos);
  }

  descartesPorMotivoEntries(): [string, number][] {
    return this.operacional ? Object.entries(this.operacional.descartesPorMotivo) : [];
  }

  solicitudesPorEstadoEntries(): [string, number][] {
    return this.operacional ? Object.entries(this.operacional.solicitudesPorEstado) : [];
  }

  exportarCsv(): void {
    this.reporteService.descargarOperacionalCsv(this.desde, this.hasta).subscribe(blob => this.descargar(blob, 'reporte-operacional.csv'));
  }

  exportarDatasetAnonimizado(): void {
    this.reporteService.descargarDatasetAnonimizadoCsv().subscribe(blob => this.descargar(blob, 'dataset-anonimizado-donantes.csv'));
  }

  // RF-27 (PDF): impresión del navegador sobre la vista de reportes (sidebar/encabezado ocultos al imprimir).
  exportarPdf(): void {
    window.print();
  }

  generarNormativoManual(): void {
    this.mensaje = '';
    this.reporteService.generarNormativoManual(this.desde, this.hasta).subscribe({
      next: () => {
        this.exito = true;
        this.mensaje = 'Reporte normativo generado correctamente.';
        this.cargarHistorialNormativo();
      },
      error: (err) => {
        this.exito = false;
        this.mensaje = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  private descargar(blob: Blob, nombreArchivo: string): void {
    const url = window.URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombreArchivo;
    enlace.click();
    window.URL.revokeObjectURL(url);
  }
}
