import { Injectable, signal } from '@angular/core';
import { AlertaService } from './alerta.service';
import { IntercambioService } from './intercambio.service';

const INTERVALO_MS = 5 * 60 * 1000;
const ESTADOS_NOTIFICABLES = new Set(['ACEPTADO', 'RECHAZADO', 'ENTREGADO', 'DEVUELTO']);

/**
 * RF-21: notificaciones ante quiebres de stock crítico, proximidad de vencimiento y
 * respuestas a solicitudes interhospitalarias.
 *
 * Implementación de referencia simplificada: usa la Notification API del navegador
 * con sondeo periódico, en vez de Web Push real (VAPID + payload push del servidor),
 * porque Web Push exige HTTPS y un endpoint público, que este entorno local de
 * desarrollo no tiene. Al instalarse como PWA (RF-20), estas notificaciones sí se
 * muestran como notificaciones nativas del sistema operativo.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionesService {

  activo = signal(false);
  private intervalId: ReturnType<typeof setInterval> | null = null;

  private clavesStockCriticoNotificadas = new Set<string>();
  private codigosVencimientoNotificados = new Set<string>();
  private estadosIntercambioConocidos = new Map<number, string>();

  constructor(private alertaService: AlertaService, private intercambioService: IntercambioService) {}

  soportado(): boolean {
    return typeof window !== 'undefined' && 'Notification' in window;
  }

  permisoConcedido(): boolean {
    return this.soportado() && Notification.permission === 'granted';
  }

  async activar(): Promise<void> {
    if (!this.soportado() || this.intervalId) return;

    if (Notification.permission === 'default') {
      await Notification.requestPermission();
    }
    if (Notification.permission !== 'granted') return;

    this.activo.set(true);
    this.verificar();
    this.intervalId = setInterval(() => this.verificar(), INTERVALO_MS);
  }

  private verificar(): void {
    this.alertaService.stockCritico().subscribe(alertas => {
      alertas.forEach(a => {
        const clave = `${a.tipoHemocomponente}|${a.grupoAbo}|${a.factorRh}`;
        if (!this.clavesStockCriticoNotificadas.has(clave)) {
          this.clavesStockCriticoNotificadas.add(clave);
          this.notificar('⚠ Quiebre de stock crítico',
            `${a.tipoHemocomponente} ${a.grupoAbo}${a.factorRh === 'POSITIVO' ? '+' : '-'}: solo ${a.unidadesDisponibles} unidad(es) disponibles.`);
        }
      });
    });

    this.alertaService.unidadesPorVencer().subscribe(alertas => {
      alertas.forEach(a => {
        if (!this.codigosVencimientoNotificados.has(a.codigoProductoIsbt)) {
          this.codigosVencimientoNotificados.add(a.codigoProductoIsbt);
          this.notificar('⏰ Unidad próxima a vencer', `${a.codigoProductoIsbt} (${a.tipoHemocomponente}) vence pronto.`);
        }
      });
    });

    this.intercambioService.listarIntercambios().subscribe(intercambios => {
      intercambios.forEach(i => {
        const anterior = this.estadosIntercambioConocidos.get(i.id);
        if (anterior && anterior !== i.estado && ESTADOS_NOTIFICABLES.has(i.estado)) {
          this.notificar('🔄 Actualización de intercambio interhospitalario', `${i.codigoProductoIsbt}: ahora está ${i.estado}.`);
        }
        this.estadosIntercambioConocidos.set(i.id, i.estado);
      });
    });
  }

  private notificar(titulo: string, cuerpo: string): void {
    try {
      new Notification(titulo, { body: cuerpo, icon: 'icons/icon-96x96.png' });
    } catch {
      // Algunos navegadores exigen mostrar notificaciones vía Service Worker en PWA instaladas;
      // si el constructor directo falla, se omite en silencio en vez de romper el flujo.
    }
  }
}
