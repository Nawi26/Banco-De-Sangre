import { ResumenExistencias } from './inventario.model';

export interface Dashboard {
  unidadesDisponibles: number;
  resumenPorGrupo: ResumenExistencias[];
  totalDonantes: number;
  donacionesUltimos30Dias: number;
  solicitudesPendientes: number;
  solicitudesPorPrioridad: Record<string, number>;
  transfusionesRealizadas: number;
  transfusionesConReaccionAdversa: number;
  unidadesPorVencerEn7Dias: number;
  intercambiosActivos: number;
}
