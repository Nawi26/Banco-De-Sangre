import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ClinicoService } from '../../../core/services/clinico.service';
import {
  CampanaDonacion, CitaDonacion, Donacion, Donante, EventoAdversoDonacion,
  MARCADORES_SEROLOGICOS, ResultadoMarcadorInput, ResultadoTriaje, TamizajeSerologico
} from '../../../core/models/clinico.model';

@Component({
  selector: 'app-donaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './donaciones.component.html'
})
export class DonacionesComponent implements OnInit {
  pestanaActiva: 'donantes' | 'diferimiento' | 'donaciones' | 'serologia' | 'campanas' | 'citas' | 'eventosAdversos' = 'donantes';

  donantes: Donante[] = [];
  donaciones: Donacion[] = [];
  eventosAdversosDonacion: EventoAdversoDonacion[] = [];

  nuevoDonante = { tipoDoc: 'DNI', numDoc: '', nombres: '', apellidos: '', fechaNacimiento: null as string | null, sexo: '', grupoAbo: 'O', factorRh: 'POSITIVO' };
  mensajeDonante = '';
  exitoDonante = false;

  nuevaDonacion = { donanteId: null as number | null, volumenMl: 450, tipoDonacion: 'VOLUNTARIA' };
  mensajeDonacion = '';
  exitoDonacion = false;

  // RF-03/RF-04: triaje clínico-epidemiológico previo a la donación.
  donanteTriajeId: number | null = null;
  cuestionarioTriaje = {
    pesoKg: null as number | null, tallaCm: null as number | null,
    presionSistolica: null as number | null, presionDiastolica: null as number | null,
    pulso: null as number | null, hemoglobina: null as number | null,
    antecedenteIts: false, antecedenteUsoDrogas: false, tatuajeOPerforacionReciente: false,
    embarazoOPartoReciente: false, viajeZonaEndemica: false, otrosAntecedentes: ''
  };
  resultadoTriaje: ResultadoTriaje | null = null;
  mensajeTriaje = '';
  exitoTriaje = false;

  // RF-38: consentimiento informado digital previo a la extracción.
  tipoValidacionConsentimiento = 'FIRMA_DIGITAL';
  evidenciaValidacionConsentimiento = '';
  mensajeConsentimiento = '';
  exitoConsentimiento = false;

  nuevoEventoAdverso = { donacionId: null as number | null, tipoEvento: 'MAREO', gravedad: 'LEVE', descripcion: '', accionesTomadas: '' };
  mensajeEventoAdverso = '';
  exitoEventoAdverso = false;

  // RF-07/RF-08/RF-31: tamizaje serológico de 7 marcadores, doble digitación ciega.
  marcadores = MARCADORES_SEROLOGICOS;
  donacionSerologiaId: number | null = null;
  tamizaje: TamizajeSerologico | null = null;
  cargandoTamizaje = false;
  resultadosDigitacion: Record<string, string> = {};
  claveConfirmacionTamizaje = '';
  mensajeTamizaje = '';
  exitoTamizaje = false;

  // RF-23/RF-35: campañas externas de captación de donantes.
  campanas: CampanaDonacion[] = [];
  nuevaCampana = { nombre: '', institucion: '', tipo: 'UNIVERSIDAD', fechaInicio: null as string | null, fechaFin: null as string | null, metaUnidades: 50 };
  mensajeCampana = '';
  exitoCampana = false;

  // RF-22: citas de donación con recordatorio automático.
  citas: CitaDonacion[] = [];
  citasProximas: CitaDonacion[] = [];
  nuevaCita = { donanteId: null as number | null, campanaId: null as number | null, fechaHora: '', notas: '' };
  mensajeCita = '';
  exitoCita = false;

  constructor(private clinicoService: ClinicoService, public authService: AuthService) {}

  ngOnInit(): void {
    this.cargarDonantes();
    this.cargarDonaciones();
    this.cargarEventosAdversosDonacion();
    this.cargarCampanas();
    this.cargarCitas();
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

  // ---------- RF-03/RF-04: Triaje clínico ----------
  donantesDiferidos(): Donante[] {
    return this.donantes.filter(d => d.estadoDiferido);
  }

  seleccionarDonanteTriaje(): void {
    this.resultadoTriaje = null;
    this.mensajeTriaje = '';
    this.mensajeConsentimiento = '';
  }

  evaluarTriaje(): void {
    this.mensajeTriaje = '';
    if (!this.donanteTriajeId) return;

    this.clinicoService.evaluarTriaje(this.donanteTriajeId, this.cuestionarioTriaje).subscribe({
      next: (resultado) => {
        this.exitoTriaje = true;
        this.resultadoTriaje = resultado;
        this.mensajeTriaje = resultado.apto ? 'Donante apto para la extracción.' : 'Donante diferido — no procede la extracción.';
        this.cargarDonantes();
      },
      error: (err) => {
        this.exitoTriaje = false;
        this.mensajeTriaje = err.error?.mensaje ?? 'No se pudo evaluar el triaje.';
      }
    });
  }

  // ---------- RF-38: Consentimiento informado ----------
  registrarConsentimiento(): void {
    this.mensajeConsentimiento = '';
    if (!this.donanteTriajeId) return;

    this.clinicoService.registrarConsentimiento(this.donanteTriajeId, {
      tipoValidacion: this.tipoValidacionConsentimiento,
      evidenciaValidacion: this.evidenciaValidacionConsentimiento,
      aceptado: true
    }).subscribe({
      next: () => {
        this.exitoConsentimiento = true;
        this.mensajeConsentimiento = 'Consentimiento informado registrado — ya puede registrarse la donación.';
        this.evidenciaValidacionConsentimiento = '';
      },
      error: (err) => {
        this.exitoConsentimiento = false;
        this.mensajeConsentimiento = err.error?.mensaje ?? 'No se pudo registrar el consentimiento.';
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

  // ---------- RF-07/RF-08/RF-31: Tamizaje serológico ----------
  seleccionarDonacionSerologia(): void {
    this.mensajeTamizaje = '';
    this.tamizaje = null;
    this.resultadosDigitacion = {};
    if (!this.donacionSerologiaId) return;

    this.cargandoTamizaje = true;
    this.clinicoService.obtenerTamizaje(this.donacionSerologiaId).subscribe({
      next: (datos) => {
        this.tamizaje = datos;
        this.cargandoTamizaje = false;
      },
      error: () => {
        // 404: aún no existe tamizaje para esta donación — se registrará la primera digitación.
        this.tamizaje = null;
        this.cargandoTamizaje = false;
      }
    });
  }

  faseTamizaje(): 'PRIMERA' | 'SEGUNDA' | 'DISCORDANCIA' | 'CONFIRMADO' {
    if (!this.tamizaje) return 'PRIMERA';
    if (this.tamizaje.estado === 'PENDIENTE_SEGUNDA_DIGITACION') return 'SEGUNDA';
    if (this.tamizaje.estado === 'DISCORDANTE') return 'DISCORDANCIA';
    return 'CONFIRMADO';
  }

  marcadoresDiscordantes(): string[] {
    if (!this.tamizaje) return [];
    return this.tamizaje.resultados.filter(r => r.concordante === false).map(r => r.marcador);
  }

  private construirResultados(marcadores: string[]): ResultadoMarcadorInput[] {
    return marcadores.map(m => ({ marcador: m, resultado: this.resultadosDigitacion[m] || 'NO_REACTIVO' }));
  }

  enviarDigitacion(): void {
    this.mensajeTamizaje = '';
    if (!this.donacionSerologiaId) return;
    const fase = this.faseTamizaje();
    const request = {
      resultados: this.construirResultados(this.marcadores.map(m => m.valor)),
      origen: 'MANUAL',
      claveConfirmacion: this.claveConfirmacionTamizaje
    };

    const llamada = fase === 'SEGUNDA'
      ? this.clinicoService.segundaDigitacionTamizaje(this.donacionSerologiaId, request)
      : this.clinicoService.primeraDigitacionTamizaje(this.donacionSerologiaId, request);

    llamada.subscribe({
      next: (datos) => {
        this.exitoTamizaje = true;
        this.tamizaje = datos;
        this.mensajeTamizaje = fase === 'SEGUNDA' ? 'Segunda digitación registrada.' : 'Primera digitación registrada — pendiente la segunda.';
        this.resultadosDigitacion = {};
        this.claveConfirmacionTamizaje = '';
        this.cargarDonaciones();
      },
      error: (err) => {
        this.exitoTamizaje = false;
        this.mensajeTamizaje = err.error?.mensaje ?? 'No se pudo registrar la digitación.';
      }
    });
  }

  resolverDiscordancia(): void {
    this.mensajeTamizaje = '';
    if (!this.donacionSerologiaId) return;
    const discordantes = this.marcadoresDiscordantes();

    this.clinicoService.resolverDiscordanciaTamizaje(this.donacionSerologiaId, {
      resultadosDefinitivos: this.construirResultados(discordantes),
      claveConfirmacion: this.claveConfirmacionTamizaje
    }).subscribe({
      next: (datos) => {
        this.exitoTamizaje = true;
        this.tamizaje = datos;
        this.mensajeTamizaje = 'Discordancia resuelta — tamizaje confirmado.';
        this.resultadosDigitacion = {};
        this.claveConfirmacionTamizaje = '';
        this.cargarDonaciones();
      },
      error: (err) => {
        this.exitoTamizaje = false;
        this.mensajeTamizaje = err.error?.mensaje ?? 'No se pudo resolver la discordancia.';
      }
    });
  }

  // ---------- RF-23/RF-35: Campañas de captación ----------
  cargarCampanas(): void {
    this.clinicoService.listarCampanas().subscribe(datos => this.campanas = datos);
  }

  progresoCampana(c: CampanaDonacion): number {
    if (!c.metaUnidades) return 0;
    return Math.min(100, Math.round((c.unidadesLogradas / c.metaUnidades) * 100));
  }

  registrarCampana(): void {
    this.mensajeCampana = '';
    this.clinicoService.crearCampana(this.nuevaCampana).subscribe({
      next: () => {
        this.exitoCampana = true;
        this.mensajeCampana = 'Campaña registrada correctamente.';
        this.nuevaCampana = { nombre: '', institucion: '', tipo: 'UNIVERSIDAD', fechaInicio: null, fechaFin: null, metaUnidades: 50 };
        this.cargarCampanas();
      },
      error: (err) => {
        this.exitoCampana = false;
        this.mensajeCampana = err.error?.mensaje ?? 'No se pudo registrar la campaña.';
      }
    });
  }

  // ---------- RF-22: Citas de donación ----------
  cargarCitas(): void {
    this.clinicoService.listarCitas().subscribe(datos => this.citas = datos);
    this.clinicoService.citasProximas(48).subscribe(datos => this.citasProximas = datos);
  }

  registrarCita(): void {
    this.mensajeCita = '';
    if (!this.nuevaCita.donanteId || !this.nuevaCita.fechaHora) return;

    this.clinicoService.crearCita(this.nuevaCita as any).subscribe({
      next: () => {
        this.exitoCita = true;
        this.mensajeCita = 'Cita programada correctamente.';
        this.nuevaCita = { donanteId: null, campanaId: null, fechaHora: '', notas: '' };
        this.cargarCitas();
      },
      error: (err) => {
        this.exitoCita = false;
        this.mensajeCita = err.error?.mensaje ?? 'No se pudo programar la cita.';
      }
    });
  }

  cambiarEstadoCita(id: number, estado: string): void {
    this.clinicoService.reprogramarCita(id, { fechaHora: null, estado, notas: '' }).subscribe(() => this.cargarCitas());
  }
}
