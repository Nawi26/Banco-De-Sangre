import { Injectable, signal } from '@angular/core';

export type Idioma = 'es' | 'en';

const CLAVE_IDIOMA = 'idioma';
const CLAVE_ALTO_CONTRASTE = 'altoContraste';

// RF-37/RNF-08: cambio de idioma y modo de alto contraste (turnos nocturnos / baja visión).
const DICCIONARIO: Record<string, Record<Idioma, string>> = {
  'nav.dashboard': { es: 'Dashboard', en: 'Dashboard' },
  'nav.inventario': { es: 'Inventario ISBT 128', en: 'ISBT 128 Inventory' },
  'nav.red': { es: 'Red Interhospitalaria', en: 'Interhospital Network' },
  'nav.donaciones': { es: 'Donantes y Donaciones', en: 'Donors and Donations' },
  'nav.clinica': { es: 'Atención Clínica', en: 'Clinical Care' },
  'nav.reportes': { es: 'Reportes', en: 'Reports' },
  'nav.soporte': { es: 'Soporte', en: 'Support' },
  'nav.medicos': { es: 'Gestión de Médicos', en: 'Doctor Management' },
  'nav.administracion': { es: 'Administración', en: 'Administration' },
  'nav.cerrarSesion': { es: 'Cerrar Sesión', en: 'Log Out' },
  'header.panelAdmin': { es: 'Panel de Administrador', en: 'Administrator Panel' },
  'header.panelMedico': { es: 'Panel Médico', en: 'Medical Panel' },
  'login.titulo': { es: 'Acceso Institucional', en: 'Institutional Access' },
  'login.subtitulo': { es: 'Hospital de Lima Este Vitarte', en: 'Lima Este Vitarte Hospital' },
  'login.identificador': { es: 'DNI o Correo Institucional', en: 'National ID or Institutional Email' },
  'login.password': { es: 'Contraseña', en: 'Password' },
  'login.ingresar': { es: 'Ingresar al Sistema', en: 'Log In' },
  'login.validando': { es: 'Validando credenciales...', en: 'Validating credentials...' },
  'accesibilidad.altoContraste': { es: 'Alto Contraste', en: 'High Contrast' },
};

@Injectable({ providedIn: 'root' })
export class AccesibilidadService {

  idioma = signal<Idioma>(this.leerIdiomaAlmacenado());
  altoContraste = signal<boolean>(this.leerAltoContrasteAlmacenado());

  constructor() {
    this.aplicarAltoContraste(this.altoContraste());
  }

  cambiarIdioma(idioma: Idioma): void {
    this.idioma.set(idioma);
    try { localStorage.setItem(CLAVE_IDIOMA, idioma); } catch { /* almacenamiento no disponible */ }
  }

  alternarAltoContraste(): void {
    const nuevoValor = !this.altoContraste();
    this.altoContraste.set(nuevoValor);
    this.aplicarAltoContraste(nuevoValor);
    try { localStorage.setItem(CLAVE_ALTO_CONTRASTE, String(nuevoValor)); } catch { /* almacenamiento no disponible */ }
  }

  t(clave: string): string {
    return DICCIONARIO[clave]?.[this.idioma()] ?? clave;
  }

  private aplicarAltoContraste(activo: boolean): void {
    document.documentElement.setAttribute('data-tema', activo ? 'alto-contraste' : 'normal');
  }

  private leerIdiomaAlmacenado(): Idioma {
    try {
      const guardado = localStorage.getItem(CLAVE_IDIOMA);
      return guardado === 'en' ? 'en' : 'es';
    } catch {
      return 'es';
    }
  }

  private leerAltoContrasteAlmacenado(): boolean {
    try {
      return localStorage.getItem(CLAVE_ALTO_CONTRASTE) === 'true';
    } catch {
      return false;
    }
  }
}
