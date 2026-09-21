import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard.service';
import { AlertaService } from '../../../core/services/alerta.service';
import { Dashboard } from '../../../core/models/dashboard.model';
import { AlertaStockCritico, AlertaVencimiento } from '../../../core/models/alerta.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  datos: Dashboard | null = null;
  alertasVencimiento: AlertaVencimiento[] = [];
  alertasStockCritico: AlertaStockCritico[] = [];

  constructor(private dashboardService: DashboardService, private alertaService: AlertaService) {}

  ngOnInit(): void {
    this.dashboardService.obtenerResumen().subscribe(datos => this.datos = datos);
    this.alertaService.unidadesPorVencer().subscribe(datos => this.alertasVencimiento = datos);
    this.alertaService.stockCritico().subscribe(datos => this.alertasStockCritico = datos);
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
