import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard.service';
import { Dashboard } from '../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  datos: Dashboard | null = null;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.obtenerResumen().subscribe(datos => this.datos = datos);
  }

  signoRh(factorRh: string): string {
    return factorRh === 'POSITIVO' ? '+' : '-';
  }

  porcentaje(valor: number, maximo: number): number {
    return maximo > 0 ? Math.round((valor / maximo) * 100) : 0;
  }

  maxUnidades(): number {
    if (!this.datos) return 0;
    return Math.max(...this.datos.resumenPorGrupo.map(r => r.unidades), 1);
  }

  maxPrioridad(): number {
    if (!this.datos) return 0;
    return Math.max(...Object.values(this.datos.solicitudesPorPrioridad), 1);
  }

  prioridadEntries(): [string, number][] {
    return this.datos ? Object.entries(this.datos.solicitudesPorPrioridad) : [];
  }
}
