import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../core/services/dashboard.service';
import { NotificacionesService } from '../../../core/services/notificaciones.service';
import { Dashboard } from '../../../core/models/dashboard.model';

// RF-20/RF-21: la app móvil es la misma PWA responsive del panel — esta pantalla
// resume, dentro del propio panel, lo que un usuario ve al instalarla en su teléfono.
@Component({
  selector: 'app-movil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app-movil.component.html'
})
export class AppMovilComponent implements OnInit {
  datos: Dashboard | null = null;

  constructor(private dashboardService: DashboardService, public notificaciones: NotificacionesService) {}

  ngOnInit(): void {
    this.dashboardService.obtenerResumen().subscribe(datos => this.datos = datos);
  }

  signoRh(factorRh: string): string {
    return factorRh === 'POSITIVO' ? '+' : '-';
  }
}
