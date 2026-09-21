import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicoService } from '../../../core/services/clinico.service';
import { Donacion, Donante, EventoAdversoDonacion } from '../../../core/models/clinico.model';

@Component({
  selector: 'app-donaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './donaciones.component.html'
})
export class DonacionesComponent implements OnInit {
  pestanaActiva: 'donantes' | 'donaciones' | 'eventosAdversos' = 'donantes';

  donantes: Donante[] = [];
  donaciones: Donacion[] = [];
  eventosAdversosDonacion: EventoAdversoDonacion[] = [];

  nuevoDonante = { tipoDoc: 'DNI', numDoc: '', nombres: '', apellidos: '', fechaNacimiento: null as string | null, sexo: '', grupoAbo: 'O', factorRh: 'POSITIVO' };
  mensajeDonante = '';
  exitoDonante = false;

  nuevaDonacion = { donanteId: null as number | null, volumenMl: 450, tipoDonacion: 'VOLUNTARIA' };
  mensajeDonacion = '';
  exitoDonacion = false;

  nuevoEventoAdverso = { donacionId: null as number | null, tipoEvento: 'MAREO', gravedad: 'LEVE', descripcion: '', accionesTomadas: '' };
  mensajeEventoAdverso = '';
  exitoEventoAdverso = false;

  constructor(private clinicoService: ClinicoService) {}

  ngOnInit(): void {
    this.cargarDonantes();
    this.cargarDonaciones();
    this.cargarEventosAdversosDonacion();
  }

  cargarDonantes(): void {
    this.clinicoService.listarDonantes().subscribe(datos => this.donantes = datos);
  }

  cargarDonaciones(): void {
    this.clinicoService.listarDonaciones().subscribe(datos => this.donaciones = datos);
  }

  registrarDonante(): void {
    this.mensajeDonante = '';
    this.clinicoService.crearDonante(this.nuevoDonante).subscribe({
      next: () => {
        this.exitoDonante = true;
        this.mensajeDonante = 'Donante registrado correctamente.';
        this.nuevoDonante = { tipoDoc: 'DNI', numDoc: '', nombres: '', apellidos: '', fechaNacimiento: null, sexo: '', grupoAbo: 'O', factorRh: 'POSITIVO' };
        this.cargarDonantes();
      },
      error: (err) => {
        this.exitoDonante = false;
        this.mensajeDonante = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  registrarDonacion(): void {
    this.mensajeDonacion = '';
    if (!this.nuevaDonacion.donanteId) return;

    this.clinicoService.crearDonacion(this.nuevaDonacion as any).subscribe({
      next: () => {
        this.exitoDonacion = true;
        this.mensajeDonacion = 'Donación registrada correctamente.';
        this.nuevaDonacion = { donanteId: null, volumenMl: 450, tipoDonacion: 'VOLUNTARIA' };
        this.cargarDonaciones();
      },
      error: (err) => {
        this.exitoDonacion = false;
        this.mensajeDonacion = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }

  // RF-43: eventos adversos ocurridos durante o después de la donación
  cargarEventosAdversosDonacion(): void {
    this.clinicoService.listarEventosAdversosDonacion().subscribe(datos => this.eventosAdversosDonacion = datos);
  }

  registrarEventoAdverso(): void {
    this.mensajeEventoAdverso = '';
    if (!this.nuevoEventoAdverso.donacionId) return;

    const donacionId = this.nuevoEventoAdverso.donacionId;
    this.clinicoService.registrarEventoAdversoDonacion(donacionId, {
      tipoEvento: this.nuevoEventoAdverso.tipoEvento,
      gravedad: this.nuevoEventoAdverso.gravedad,
      descripcion: this.nuevoEventoAdverso.descripcion,
      accionesTomadas: this.nuevoEventoAdverso.accionesTomadas
    }).subscribe({
      next: () => {
        this.exitoEventoAdverso = true;
        this.mensajeEventoAdverso = 'Evento adverso registrado correctamente.';
        this.nuevoEventoAdverso = { donacionId: null, tipoEvento: 'MAREO', gravedad: 'LEVE', descripcion: '', accionesTomadas: '' };
        this.cargarEventosAdversosDonacion();
      },
      error: (err) => {
        this.exitoEventoAdverso = false;
        this.mensajeEventoAdverso = err.error?.mensaje ?? 'No se pudo contactar con el servidor.';
      }
    });
  }
}
