/* =====================================================================
   Validaciones y máscaras de entrada del Sistema Web de Gestión de Banco
   de Sangre. Se cargan en TODAS las pantallas (layout) y en el login.

   Cómo se usa:
     1) En el HTML, cada campo lleva data-val="tipo" (dni, nombre, fecha...).
        Mientras se escribe, la máscara corrige el texto: el DNI solo admite
        8 dígitos, el nombre solo letras, la fecha agrega las "/" sola, etc.
     2) Al salir del campo se valida y aparece el mensaje debajo.
     3) Al guardar, cada función llama a VAL.formulario([...]) que revisa
        todos los campos, marca los que están mal y avisa del primero.
   El servidor vuelve a validar (ValidadorDatos.java): la validación del
   navegador ayuda al usuario, la del servidor es la que protege la base.
   ===================================================================== */
const VAL = (function () {
  const L = 'A-Za-zÁÉÍÓÚÜÑáéíóúüñ';
  const DIAS_MS = 86400000;

  const hoy = () => { const d = new Date(); d.setHours(0, 0, 0, 0); return d; };

  /** "dd/mm/aaaa" -> Date si es una fecha real del calendario; si no, null */
  function fechaReal(txt) {
    const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(txt || '');
    if (!m) return null;
    const d = +m[1], mo = +m[2], y = +m[3];
    const f = new Date(y, mo - 1, d);
    if (f.getFullYear() !== y || f.getMonth() !== mo - 1 || f.getDate() !== d || y < 1900) return null;
    return f;
  }
  function edadEn(f) {
    const h = hoy();
    let e = h.getFullYear() - f.getFullYear();
    if (h.getMonth() < f.getMonth() || (h.getMonth() === f.getMonth() && h.getDate() < f.getDate())) e--;
    return e;
  }
  const titulo = s => s.toLowerCase().replace(/(^|\s)([a-záéíóúüñ])/g, (m, a, b) => a + b.toUpperCase());
  const num = s => parseFloat(String(s).replace(',', '.'));
  const opt = (el, k, def) => (el && el.dataset && el.dataset[k] !== undefined) ? el.dataset[k] : def;

  /* Cada tipo: mask(valor, elemento, evento) -> texto corregido mientras se escribe
                permite: regex de UN carácter aceptado (bloquea teclas inválidas)
                normaliza(valor) -> texto final al salir del campo
                check(valor, elemento) -> true o el mensaje de error */
  const tipos = {
    dni: {
      permite: /^\d$/,
      mask: v => v.replace(/\D/g, '').slice(0, 8),
      check: v => /^\d{8}$/.test(v) || 'El DNI debe tener exactamente 8 dígitos',
    },
    nombre: {
      permite: new RegExp('^[' + L + ' ]$'),
      mask: v => v.replace(new RegExp('[^' + L + ' ]', 'g'), '').replace(/^\s+/, '').replace(/\s{2,}/g, ' ').slice(0, 60),
      normaliza: v => titulo(v.trim().replace(/\s{2,}/g, ' ')),
      check: v => {
        const p = v.trim().split(/\s+/).filter(Boolean);
        if (p.length < 2) return 'Escribe nombres y apellidos completos (solo letras)';
        if (p.some(x => x.length < 2)) return 'Cada nombre o apellido debe tener al menos 2 letras';
        return true;
      },
    },
    fecha: {
      permite: /^\d$/,
      mask: (v, el, ev) => {
        const d = v.replace(/\D/g, '').slice(0, 8);
        const borrando = ev && ev.inputType && ev.inputType.startsWith('delete');
        let o = d.slice(0, 2);
        if (d.length > 2) o += '/' + d.slice(2, 4);
        if (d.length > 4) o += '/' + d.slice(4);
        // las "/" aparecen solas al completar día y mes (pero se puede borrar hacia atrás)
        if (!borrando && (d.length === 2 || d.length === 4)) o += '/';
        return o;
      },
      check: v => fechaReal(v) ? true : 'Escribe una fecha válida con el formato dd/mm/aaaa',
    },
    fechaNacimiento: {
      permite: /^\d$/,
      mask: (v, el, ev) => tipos.fecha.mask(v, el, ev),
      check: v => {
        const f = fechaReal(v);
        if (!f) return 'Escribe una fecha válida con el formato dd/mm/aaaa';
        if (f > hoy()) return 'La fecha de nacimiento no puede ser futura';
        const e = edadEn(f);
        if (e < 18) return 'El donante debe ser mayor de 18 años (tiene ' + e + ')';
        if (e > 65) return 'El donante no puede superar los 65 años (tiene ' + e + ')';
        return true;
      },
    },
    fechaFutura: {
      permite: /^\d$/,
      mask: (v, el, ev) => tipos.fecha.mask(v, el, ev),
      check: v => {
        const f = fechaReal(v);
        if (!f) return 'Escribe una fecha válida con el formato dd/mm/aaaa';
        if (f < hoy()) return 'La fecha límite no puede ser anterior a hoy';
        if (f - hoy() > 365 * DIAS_MS) return 'La reserva no puede pasar de 1 año';
        return true;
      },
    },
    hc: {
      permite: /^[A-Za-z0-9-]$/,
      mask: v => {
        const t = v.toUpperCase();
        if (/^H?C?-?$/.test(t)) return t.slice(0, 3);
        const d = t.replace(/^HC-?/, '').replace(/\D/g, '').slice(0, 9);
        return 'HC-' + d.slice(0, 4) + (d.length > 4 ? '-' + d.slice(4) : '');
      },
      check: v => /^HC-\d{4}-\d{5}$/.test(v) || 'La historia clínica debe tener el formato HC-AAAA-00000',
    },
    muestra: {
      permite: /^[A-Za-z0-9-]$/,
      mask: v => {
        const t = v.toUpperCase();
        if (/^S?-?$/.test(t)) return t.slice(0, 2);
        return 'S-' + t.replace(/^S-?/, '').replace(/\D/g, '').slice(0, 5);
      },
      check: v => /^S-\d{5}$/.test(v) || 'El número de muestra debe tener el formato S-00000',
    },
    cie10: {
      mask: v => v.replace(new RegExp('[^' + L + '0-9 .,()—–-]', 'g'), '').replace(/^\s+/, '').replace(/\s{2,}/g, ' ').slice(0, 90)
                  .replace(/^([a-z])/, m => m.toUpperCase()),
      normaliza: v => v.trim().replace(/\s[-–]\s/, ' — ').replace(/^([a-z])/, m => m.toUpperCase()),
      check: v => /^[A-Z]\d{2}(\.\d{1,2})?(\s[—–-]\s\S.{2,})?$/.test(v.trim()) ||
        'Usa un código CIE-10 válido, por ejemplo K92.2 — Hemorragia digestiva alta',
    },
    componente: {
      mask: v => v.replace(new RegExp('[^' + L + '0-9 .,()—–-]', 'g'), '').replace(/^\s+/, '').replace(/\s{2,}/g, ' ').slice(0, 80),
      normaliza: v => v.trim().replace(/\s[-–]\s/, ' — '),
      check: v => /^([1-9]|1\d|20)\s+unidad(es)?\s+[—–-]\s+\S.{3,}$/i.test(v.trim()) ||
        'Usa el formato: 2 unidades — Concentrado de Hematíes (de 1 a 20 unidades)',
    },
    correo: {
      permite: /^[A-Za-z0-9._@-]$/,
      mask: v => v.toLowerCase().replace(/[^a-z0-9._@-]/g, '').slice(0, 60),
      check: v => /^[a-z0-9._-]+@hlev\.gob\.pe$/.test(v) || 'Usa el correo institucional (usuario@hlev.gob.pe)',
    },
    usuarioLogin: {
      mask: v => /^\d/.test(v) ? v.replace(/\D/g, '').slice(0, 8) : v.toLowerCase().replace(/[^a-z0-9._@-]/g, '').slice(0, 60),
      check: v => (/^\d{8}$/.test(v) || /^[a-z0-9._-]+@hlev\.gob\.pe$/.test(v)) ||
        'Ingresa tu DNI (8 dígitos) o tu correo institucional @hlev.gob.pe',
    },
    clave: {
      mask: v => v.replace(/\s/g, '').slice(0, 40),
      check: v => v.length >= 6 || 'La contraseña debe tener al menos 6 caracteres',
    },
    peso: {
      permite: /^[\d.,]$/,
      mask: v => {
        let t = v.replace(',', '.').replace(/[^\d.]/g, '');
        const p = t.split('.');
        t = p[0].slice(0, 3) + (p.length > 1 ? '.' + p.slice(1).join('').slice(0, 1) : '');
        return t;
      },
      check: v => { const n = num(v); return (n >= 30 && n <= 250) || 'El peso debe estar entre 30 y 250 kg'; },
    },
    hemoglobina: {
      permite: /^[\d.,]$/,
      mask: v => {
        let t = v.replace(',', '.').replace(/[^\d.]/g, '');
        const p = t.split('.');
        t = p[0].slice(0, 2) + (p.length > 1 ? '.' + p.slice(1).join('').slice(0, 1) : '');
        return t;
      },
      check: v => { const n = num(v); return (n >= 5 && n <= 22) || 'La hemoglobina debe estar entre 5.0 y 22.0 g/dL'; },
    },
    entero: {
      permite: /^\d$/,
      mask: v => v.replace(/\D/g, '').slice(0, 3),
      check: (v, el) => {
        const n = parseInt(v, 10), min = +opt(el, 'min', 1), max = +opt(el, 'max', 99);
        return (n >= min && n <= max) || 'Ingresa un número entero entre ' + min + ' y ' + max;
      },
    },
    texto: {
      mask: (v, el) => v.replace(/[<>{}[\]\\`^|~\u0000-\u001f]/g, '').replace(/^\s+/, '').replace(/\s{2,}/g, ' ')
                        .slice(0, +opt(el, 'max', 120)),
      normaliza: v => v.trim(),
      check: (v, el) => {
        const min = +opt(el, 'min', 3);
        if (v.trim().length < min) return 'Escribe al menos ' + min + ' caracteres';
        if (!new RegExp('[' + L + ']{3}').test(v)) return 'Describe el dato con palabras (debe contener letras)';
        return true;
      },
    },
    buscar: {
      mask: v => v.replace(/[<>{}[\]\\`^|~\u0000-\u001f]/g, '').replace(/^\s+/, '').slice(0, 40),
      check: () => true,
    },
  };

  /* ---------- mensajes y marcas visuales ---------- */
  function marcar(el, msg) {
    el.classList.toggle('input-error', !!msg);
    el.setAttribute('aria-invalid', msg ? 'true' : 'false');
    let aviso = el.parentElement && el.parentElement.querySelector('.field-error[data-for="' + (el.id || el.name) + '"]');
    if (msg) {
      if (!aviso && el.parentElement) {
        aviso = document.createElement('div');
        aviso.className = 'field-error';
        aviso.dataset.for = el.id || el.name;
        el.insertAdjacentElement('afterend', aviso);
      }
      if (aviso) aviso.textContent = msg;
    } else if (aviso) {
      aviso.remove();
    }
  }

  /** Valida un campo. Devuelve el mensaje de error o null. */
  function campo(el, tipo, o) {
    o = o || {};
    if (!el) return null;
    tipo = tipo || el.dataset.val;
    const t = tipos[tipo];
    let v = (el.value || '').toString();
    if (t && t.normaliza && v && !o.escribiendo) { const n = t.normaliza(v); if (n !== v) { el.value = n; v = n; } }
    const obligatorio = o.req !== undefined ? o.req : el.dataset.req === '1';
    let msg = null;
    if (!v.trim()) {
      if (obligatorio) msg = 'Este campo es obligatorio';
    } else if (t) {
      const r = t.check(v, el);
      if (r !== true) msg = r;
    }
    marcar(el, msg);
    return msg;
  }

  /**
   * Valida varios campos: specs = [[id, tipo, {label, req}], ...]
   * Marca todos los erróneos, enfoca el primero y avisa con un mensaje.
   * Devuelve true si todo está bien.
   */
  function formulario(specs) {
    let primero = null;
    specs.forEach(s => {
      const el = typeof s[0] === 'string' ? document.getElementById(s[0]) : s[0];
      if (!el) return;
      const msg = campo(el, s[1], Object.assign({ req: true }, s[2] || {}));
      if (msg && !primero) primero = { el, msg, label: (s[2] && s[2].label) || '' };
    });
    if (primero) {
      const txt = (primero.label ? primero.label + ': ' : '') + primero.msg;
      if (typeof toast === 'function') toast(txt, true);
      try { primero.el.focus(); } catch (e) { /* sin foco */ }
      return false;
    }
    return true;
  }

  function limpiar(ids) {
    ids.forEach(id => { const el = document.getElementById(id); if (el) marcar(el, null); });
  }

  /* ---------- eventos globales: funcionan también en ventanas (modales) ---------- */
  document.addEventListener('beforeinput', e => {
    const el = e.target;
    const tipo = el && el.dataset && el.dataset.val;
    const t = tipo && tipos[tipo];
    if (!t || !t.permite || e.inputType !== 'insertText' || !e.data) return;
    if ([...e.data].some(c => !t.permite.test(c))) {
      e.preventDefault();            // la tecla inválida ni siquiera aparece
      el.classList.add('input-rechazo');
      setTimeout(() => el.classList.remove('input-rechazo'), 250);
    }
  });

  document.addEventListener('input', e => {
    const el = e.target;
    const tipo = el && el.dataset && el.dataset.val;
    const t = tipo && tipos[tipo];
    if (!t || typeof el.value !== 'string') return;
    const antes = el.value;
    const despues = t.mask(antes, el, e);
    if (despues !== antes) {
      const pos = el.selectionStart;
      el.value = despues;
      try {
        const nueva = (tipo.startsWith('fecha') || tipo === 'hc' || tipo === 'muestra')
          ? despues.length : Math.max(0, (pos || 0) - (antes.length - despues.length));
        el.setSelectionRange(nueva, nueva);
      } catch (err) { /* el tipo de campo no permite cursor */ }
    }
    if (el.classList.contains('input-error')) campo(el, tipo, { escribiendo: true });
  });

  document.addEventListener('focusout', e => {
    const el = e.target;
    const tipo = el && el.dataset && el.dataset.val;
    if (!tipo || !tipos[tipo] || tipo === 'buscar') return;
    if (!el.value.trim() && el.dataset.req !== '1') { marcar(el, null); return; }
    campo(el, tipo);
  });

  return { tipos, campo, formulario, limpiar, marcar, fechaReal, edadEn };
})();
