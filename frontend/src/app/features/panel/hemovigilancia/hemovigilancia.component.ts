import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicoService } from '../../../core/services/clinico.service';
import { EventoAdversoTransfusional, Transfusion } from '../../../core/models/clinico.model';

// RF-15: hemovigilancia de reacciones transfusionales, con trazabilidad hacia la donación origen.
@Component({
  selector: 'app-hemovigilancia',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hemovigilancia.component.html'
})
export class HemovigilanciaComponent implements OnInit {
  transfusiones: Transfusion[] = [];
  eventosAdversosTransfusionales: EventoAdversoTransfusional[] = [];

  nuevoEventoAdverso = { transfusionId: null as number | null, tipoReaccion: 'FEBRIL', esInmediata: true, gravedad: 'LEVE', descripcion: '', accionesTomadas: '' };
  mensajeEventoAdverso = '';
  exitoEventoAdverso = false;

  constructor(private clinicoService: ClinicoService) {}

  ngOnInit(): void {
    this.clinicoService.listarTransfusiones().subscribe(datos => this.transfusiones = datos);
    this.cargarEventosAdversosTransfusionales();
  }

  cargarEventosAdversosTransfusionales(): void {
    this.clinicoService.listarEventosAdversosTransfusionales().subscribe(datos => this.eventosAdversosTransfusionales = datos);
  }

  registrarEventoAdverso(): void {
    this.mensajeEventoAdverso = '';
    if (!this.nuevoEventoAdverso.transfusionId) return;

    const transfusionId = this.nuevoEventoAdverso.transfusionId;
    this.clinicoService.registrarEventoAdversoTransfusional(transfusionId, {
      tipoReaccion: this.nuevoEventoAdverso.tipoReaccion,
      esInmediata: this.nuevoEventoAdverso.esInmediata,
      gravedad: this.nuevoEventoAdverso.gravedad,
      descripcion: this.nuevoEventoAdverso.descripcion,
      accionesTomadas: this.nuevoEventoAdverso.accionesTomadas
    }).subscribe({
      next: () => {
        this.exitoEventoAdverso = true;
        this.mensajeEventoAdverso = 'Evento adverso registrado correctamente.';
        this.nuevoEventoAdverso = { transfusionId: null, tipoReaccion: 'FEBRIL', esInmediata: true, gravedad: 'LEVE', descripcion: '', accionesTomadas: '' };
        this.cargarEventosAdversosTransfusionales();
      },
      error: (err) => {
        this.exitoEventoAdverso = false;
        this.mensajeEventoAdverso = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }
}
