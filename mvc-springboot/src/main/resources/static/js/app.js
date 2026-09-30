/* =========================================================
   ESTADO CENTRAL DE LA APLICACIÓN (solo en memoria del navegador)
   Todas las acciones mutan este objeto y disparan re-render +
   un asiento en el log de auditoría, simulando el
   comportamiento de un sistema transaccional real.

   Los arreglos empiezan VACÍOS a propósito: los datos de ejemplo ya
   NO viven aquí, viven en el servidor (PostgreSQL, a través de los DAO) y el
   Controlador se los entrega a cada Vista, que los usa para llenar
   este mismo "state" apenas carga la página (ver el <script> al
   final de cada plantilla). Este objeto sigue existiendo para que
   las acciones interactivas (filtrar, agregar, cambiar estado, etc.)
   sigan funcionando en el navegador sin recargar la página.
========================================================= */
const state = {
  currentUser: "",
  currentRole: "",

  aboGroups: [],
  alerts: [],

  // { din, producto, abo, camara, exp, status }
  inventory: [],

  donantes: [],
  // { din, marcador, dig1, dig2, resultado, digitador, fecha }
  serologia: [],

  solicitudes: [],
  pacientes: [],
  // { solicitudId, prueba, muestra, unidad, grupoPaciente, resultado, estado, validadoPor, fecha }
  pruebas: [],
  hemovigilancia: [],

  auditLog: [],

  // Pantallas que el rol de la sesion puede ver (las entrega el servidor)
  vistasPermitidas: [],

  despachoScan: { din:null, hc:null },
  reqCounter: 340,
};

/* =========================================================
   "BASE DE DATOS" LOCAL DEL NAVEGADOR (localStorage)
   Esta etapa del proyecto no tiene un backend con base de datos real:
   cada carga de pagina recibe de nuevo los datos de ejemplo del
   servidor (PostgreSQL). Para que la aplicacion se sienta realmente
   conectada -que una solicitud creada desde la app movil aparezca
   tambien en el panel web, y que los cambios sobrevivan a navegar
   entre pantallas- usamos localStorage como una "base de datos" local:
   cada vez que ocurre una accion (ver logAudit, el punto donde TODAS
   las acciones del sistema quedan registradas) se guarda una foto de
   las colecciones mutables; cada vez que una pagina carga sus datos
   del servidor, si ya existe una version guardada en este navegador,
   esa es la que se usa (porque refleja lo que el usuario hizo antes).
========================================================= */
const LOCAL_DB_KEY = 'bancosangre_local_db';

/* MODO SERVIDOR: cuando la aplicacion corre en Spring Boot (layout.html activa
   window.PERSISTENCIA = 'servidor'), cada cambio se guarda en la base de datos
   PostgreSQL con POST /api/sincronizar y el navegador NO guarda nada por su
   cuenta. Sin esa marca (por ejemplo la version de demostracion que se abre
   sola en un navegador) los cambios se guardan en localStorage. */
const MODO_SERVIDOR = (typeof window !== 'undefined' && window.PERSISTENCIA === 'servidor');

/* Colecciones que la pantalla actual recibió del servidor. Solo esas se
   guardan: si se guardaran todas, una pantalla que no carga (por ejemplo)
   las solicitudes escribiría una lista vacía encima de las que sí existen. */
let clavesCargadas = [];

/* Colecciones que se envian a la base (el resto se lee y no se edita aqui) */
const COLECCIONES_GUARDABLES = ['usuarios','donantes','inventory','serologia','solicitudes','pruebas','hemovigilancia','intercambios'];

/* Novedades que solo se agregan (bitacora, alertas, despachos): se envian una
   vez y se vacian. */
function colasVacias(){ return { auditNuevos: [], alertasNuevas: [], despachosNuevos: [] }; }
let pendientes = colasVacias();

function leerLocal(){
  if (MODO_SERVIDOR) return {};
  try {
    const raw = localStorage.getItem(LOCAL_DB_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch (e) { return {}; }
}

let envioProgramado = false;
let colaEnvio = Promise.resolve();

function persistirEstado(){
  if (MODO_SERVIDOR) {
    // Se junta todo lo que cambio en este mismo instante y se manda una sola vez
    if (envioProgramado) return;
    envioProgramado = true;
    setTimeout(() => { envioProgramado = false; enviarAlServidor(); }, 0);
    return;
  }
  try {
    const guardado = leerLocal();
    clavesCargadas.forEach(clave => { guardado[clave] = state[clave]; });
    localStorage.setItem(LOCAL_DB_KEY, JSON.stringify(guardado));
  } catch (e) { /* localStorage no disponible: la app sigue funcionando solo con datos del servidor */ }
}

function enviarAlServidor(){
  const cambios = Object.assign({}, pendientes);
  pendientes = colasVacias();
  clavesCargadas.forEach(clave => {
    if (COLECCIONES_GUARDABLES.includes(clave)) cambios[clave] = state[clave];
  });
  const cuerpo = JSON.stringify(cambios);
  colaEnvio = colaEnvio
    .then(() => fetch('/api/sincronizar', {
      method: 'POST', credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: cuerpo, keepalive: cuerpo.length < 60000,
    }))
    .then(r => r.json().catch(() => ({ ok: false, error: 'Respuesta invalida del servidor' })))
    .then(j => {
      if (!j.ok) throw new Error(j.error || 'Error desconocido');
      // Avisa a otras pestanas abiertas (por ejemplo la app movil) que hay novedades
      try { localStorage.setItem('bancosangre_evento', String(Date.now())); } catch (e) { /* sin almacenamiento */ }
    })
    .catch(e => {
      toast('No se pudo guardar en la base de datos: ' + e.message, true);
      // La pantalla queda distinta a la base: se recarga para mostrar lo que realmente hay guardado
      setTimeout(() => location.reload(), 3500);
    });
}

/** Vuelve a pedir al servidor colecciones ya guardadas (nombre en pantalla -> nombre en la base). */
function recargarDelServidor(mapa){
  return Promise.all(Object.entries(mapa).map(([clave, coleccion]) =>
    fetch('/api/listar/' + coleccion, { credentials: 'same-origin' })
      .then(r => r.json()).then(datos => { state[clave] = datos; })
  ));
}

/**
 * Se llama en cada pantalla con el nombre de las colecciones que acaba de
 * recibir del servidor. En modo local (demo), si el navegador ya guardó una
 * versión de alguna de ellas, esa es la que vale. En modo servidor la base
 * de datos siempre manda.
 */
function sincronizarLocal(...claves){
  clavesCargadas = claves;
  if (MODO_SERVIDOR) return;
  const guardado = leerLocal();
  claves.forEach(clave => {
    if (guardado[clave] !== undefined) state[clave] = guardado[clave];
  });
}

/** Vuelve a los datos de ejemplo: borra lo guardado en este navegador. */
function reiniciarDatosLocales(){
  try { localStorage.removeItem(LOCAL_DB_KEY); } catch (e) { /* sin almacenamiento */ }
}

/** Identificador unico para registros creados en pantalla (eventos, intercambios) */
function nuevoId(){
  if (window.crypto && crypto.randomUUID) return crypto.randomUUID();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0; return (c === 'x' ? r : (r & 3 | 8)).toString(16);
  });
}

const statusMeta = {
  disponible:["green","Disponible"], reservado:["amber","Reservado"], cuarentena:["purple","Cuarentena"],
  bloqueado:["red","Bloqueado"], vencido:["slate","Vencido"], despachado:["blue","Despachado"], incinerado:["char","Incinerado"], fraccionado:["slate","Fraccionado"],
};
const serologyMarkers = ["VIH 1/2","Hepatitis B (HBsAg)","Hepatitis C","Sífilis (VDRL/RPR)","Chagas","HTLV I/II"];

/* ---------------- Utilidades ---------------- */
function pill(colorClass, label){ return `<span class="pill ${colorClass}"><span class="dot"></span>${label}</span>`; }
function nowStr(){
  const d = new Date();
  const p = n => String(n).padStart(2,'0');
  return `${p(d.getDate())}/${p(d.getMonth()+1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}
function parseFecha(txt){
  const m = /^(\d{2})\/(\d{2})\/(\d{4})/.exec(txt || '');
  return m ? new Date(+m[3], +m[2]-1, +m[1]) : null;
}
function formatoFecha(d){
  const p = n => String(n).padStart(2,'0');
  return `${p(d.getDate())}/${p(d.getMonth()+1)}/${d.getFullYear()}`;
}
function sumarDias(fecha, dias){ const d = new Date(fecha.getTime()); d.setDate(d.getDate() + dias); return d; }
function nombreDonante(dni){
  const d = state.donantes.find(x => x.dni === dni);
  return d ? d.nombre : null;
}
/* Código DIN de una donación nueva: centro + año + correlativo */
function siguienteDIN(){
  const mayor = state.inventory.reduce((m, u) => {
    const n = parseInt(u.din.split('-')[0].slice(7), 10);
    return isNaN(n) ? m : Math.max(m, n);
  }, 0);
  return 'W1237' + String(new Date().getFullYear()).slice(2) + String(mayor + 1).padStart(7, '0');
}
function randomIp(){ return `10.20.4.${Math.floor(Math.random()*250)+2}`; }

function logAudit(action, resource){
  const ip = randomIp();
  const quien = `${state.currentUser} (${state.currentRole})`;
  state.auditLog.unshift([nowStr(), quien, action, resource, ip]);
  pendientes.auditNuevos.push({ usuario: quien, accion: action, recurso: String(resource), ip });
  // En la versión SPA original solo volvia a pintar la tabla si la vista de
  // Auditoria estaba activa en la misma pagina; ahora cada vista es una ruta
  // real, asi que solo re-renderiza si el contenedor de esta pagina existe.
  if(document.getElementById('audit-rows')) renderAudit();
  // Toda accion registrada aqui es, por diseño, el punto donde algo cambio
  // de verdad en el sistema: aprovechamos ese mismo punto para guardar la
  // "base de datos" local (ver arriba) y que el cambio persista.
  persistirEstado();
}

/* ---------------- Menú lateral ----------------
   En pantallas grandes el menú está siempre a la vista (el botón ni aparece).
   En el celular se desliza sobre el contenido: arranca cerrado y se abre con
   el botón, igual que en cualquier aplicación móvil. */
function alternarMenu(){
  document.querySelector('.app')?.classList.toggle('menu-oculto');
}

function cerrarMenuEnCelular(){
  if(window.matchMedia('(max-width: 560px)').matches){
    document.querySelector('.app')?.classList.add('menu-oculto');
  }
}

function prepararMenu(){
  const app = document.querySelector('.app');
  if(!app) return;
  app.classList.toggle('menu-oculto', window.matchMedia('(max-width: 560px)').matches);
  // Tocar el velo o elegir un módulo cierra el menú
  app.addEventListener('click', e => {
    if(e.target === app) cerrarMenuEnCelular();
    if(e.target.closest('.nav-item')) cerrarMenuEnCelular();
  });
}
document.addEventListener('DOMContentLoaded', prepararMenu);

function toast(message, isError){
  const wrap = document.getElementById('toast-wrap');
  const el = document.createElement('div');
  el.className = 'toast' + (isError ? ' err' : '');
  el.textContent = message;
  wrap.appendChild(el);
  requestAnimationFrame(()=> el.classList.add('show'));
  setTimeout(()=>{ el.classList.remove('show'); setTimeout(()=>el.remove(), 250); }, 3200);
}

function openModal(html){
  document.getElementById('modal-box').innerHTML = html;
  document.getElementById('modal-overlay').classList.add('open');
}
function closeModal(){ document.getElementById('modal-overlay').classList.remove('open'); }
document.getElementById('modal-overlay').addEventListener('click', e=>{ if(e.target.id==='modal-overlay') closeModal(); });

/* Suma de verificación ISBT 128 (módulo 37-2, simplificada para la versión original) */
function computeChecksum37(digits){
  const alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  let sum = 0;
  for(let i=0;i<digits.length;i++){ sum += (alphabet.indexOf(digits[i])+1) * (i+1); }
  return alphabet[sum % 37];
}
function generarDIN(){
  const base = "1237250" + String(Math.floor(100000 + Math.random()*899999));
  const check = computeChecksum37(base);
  return "W" + base + check;
}

/* ---------------- Render: Dashboard ---------------- */
function renderAbo(){
  document.getElementById('abo-grid').innerHTML = state.aboGroups.map(([grp,units,cap,critical])=>{
    const pct = Math.max(6, Math.min(100, (units/cap)*100));
    return `<div style="padding:12px 14px; border-radius:10px; background:${critical?'var(--red-50)':'var(--bg)'};">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <span style="font-weight:700; font-size:16px;">${grp}</span>
        <span style="font-weight:600; font-size:14px; color:${critical?'var(--red-700)':'var(--ink-900)'};">${units}</span>
      </div>
      <div class="progress-track"><div class="progress-fill" style="width:${pct}%; background:${critical?'var(--red-700)':'var(--green-700)'};"></div></div>
    </div>`;
  }).join('');
}

function renderAlerts(){
  // pushAlert() se llama desde acciones que pueden ocurrir en cualquier
  // pantalla (no solo el Dashboard, que es la unica con #alerts-list);
  // sin esta guarda, la linea de abajo lanzaba un error en cualquier otra
  // vista y cortaba en seco el resto de la funcion que la llamó (por eso
  // "no pasaba nada" al enviar una solicitud o simular un push movil).
  const el = document.getElementById('alerts-list');
  if(!el) return;
  el.innerHTML = state.alerts.map(([color,title,meta])=>{
    const dotColor = {red:'var(--red-700)',amber:'var(--amber-700)',blue:'var(--blue-700)',purple:'var(--purple-700)'}[color];
    return `<div class="timeline-item"><div class="timeline-dot" style="background:${dotColor};"></div>
      <div><div class="timeline-title">${title}</div><div class="timeline-meta">${meta}</div></div></div>`;
  }).join('');
}

function pushAlert(color, title, meta){
  state.alerts.unshift([color, title, meta]);
  state.alerts = state.alerts.slice(0, 6);
  pendientes.alertasNuevas.push({ color, titulo: title, detalle: meta });
  persistirEstado();
  renderAlerts();
  // La app móvil espeja las mismas alertas del Dashboard: si esta pagina
  // define su propio render (ver movil.html), lo refrescamos tambien.
  if (typeof renderMovil === 'function') renderMovil(true);
}

/* Accesos rapidos posibles, en orden de prioridad. Cada uno apunta a una
   pantalla: al dibujarlos se filtran contra las pantallas que el rol de la
   sesion tiene permitidas (state.vistasPermitidas, que viene del servidor
   desde Rol.java), asi que un Medico Solicitante nunca ve "Despachar una
   unidad" y un Administrador no ve accesos que no le tocan. */
const quickAccessData = [
  ["Nueva solicitud de sangre","solicitudes"], ["Registrar un donante","donantes"],
  ["Etiquetar una unidad","etiquetado"], ["Despachar una unidad","despacho"],
  ["Ver el inventario","inventario"], ["Registrar una reacción","hemovigilancia"],
  ["Pedir a otro hospital","interhospitalario"], ["Abrir la app móvil","movil"],
  ["Generar un reporte","reportes"], ["Administrar usuarios","administracion"],
  ["Revisar la auditoría","auditoria"],
];
/* Si esta pagina se esta dibujando en modo movil (dentro del celular de la
   pantalla "Aplicacion movil"), los enlaces internos deben seguir en modo
   movil; si no, el celular saltaria a la version de escritorio. */
function rutaVista(vista){
  const enMovil = document.querySelector('.app-movil') !== null;
  return '/' + vista + (enMovil ? '?movil=1' : '');
}
function renderQuickAccess(){
  const permitidas = state.vistasPermitidas || [];
  const accesos = quickAccessData
    .filter(([, view]) => !permitidas.length || permitidas.includes(view))
    .slice(0, 4);
  document.getElementById('quick-access').innerHTML = accesos.map(([title,view])=>{
    return `<button class="card" style="cursor:pointer; text-align:left; padding:16px; border-radius:12px;" onclick="location.href=rutaVista('${view}')">
      <div style="font-weight:600; font-size:13.5px;">${title}</div>
    </button>`;
  }).join('');
}

/* ---------------- Render + acciones: Inventario (RF-09, RF-10) ---------------- */
function renderInventory(){
  const q = (document.getElementById('inv-search')?.value || '').trim().toLowerCase();
  const filter = document.getElementById('inv-filter')?.value || '';
  const rows = state.inventory
    .filter(u => !filter || u.status === filter)
    .filter(u => !q || u.din.toLowerCase().includes(q) || u.abo.toLowerCase().includes(q));

  const tabla = document.getElementById('inventory-rows');
  if(tabla) tabla.innerHTML = rows.map(u=>{
    const [color,label] = statusMeta[u.status];
    return `<tr>
      <td class="mono">${u.din}</td>
      <td class="cell-strong">${u.producto}</td>
      <td class="mono">${u.abo}</td>
      <td class="cell-muted">${u.camara}</td>
      <td class="mono">${u.exp}</td>
      <td>${pill(color,label)}</td>
      <td><button class="btn btn-ghost btn-sm" onclick="verUnidad('${u.din}')">Ver</button></td>
    </tr>`;
  }).join('');
  const countEl = document.getElementById('inv-count');
  if(countEl) countEl.textContent = `${rows.length} de ${state.inventory.length} unidades mostradas · ordenadas por fecha de vencimiento`;

  // refresh selects que dependen del inventario
  fillFractSelect(); fillSerologiaSelect(); fillDespachoContext();
  renderDonaciones();
}

/* Donaciones recibidas: una fila por bolsa que llegó de un donante, con su
   tamizaje y los hemocomponentes que salieron de ella. Se ve aparte de la
   lista de hemocomponentes para no mezclar datos del donante con el stock. */
function renderDonaciones(){
  const body = document.getElementById('donaciones-rows');
  if(!body) return;
  const q = (document.getElementById('don-inv-buscar')?.value || '').trim().toLowerCase();
  const bolsas = state.inventory.filter(u => !u.din.includes('-') && u.donante && u.donante !== '—');
  const filas = bolsas.filter(u => {
    const nombre = (nombreDonante(u.donante) || '').toLowerCase();
    return !q || nombre.includes(q) || u.donante.includes(q) || u.din.toLowerCase().includes(q);
  });
  body.innerHTML = filas.length ? filas.map(u => {
    const sero = state.serologia.filter(r => r.din === u.din);
    const malos = sero.filter(r => r.resultado !== 'No reactivo');
    let tamizaje;
    if(malos.length) tamizaje = pill('red', 'No conforme: ' + malos.map(r => r.marcador).join(', '));
    else if(sero.length) tamizaje = pill('green', 'Conforme');
    else if(u.status === 'cuarentena' || u.status === 'fraccionado') tamizaje = pill('purple', 'Pendiente');
    else tamizaje = pill('slate', 'Anterior al sistema');
    const hijos = state.inventory.filter(h => h.din.startsWith(u.din + '-'));
    const obtenidos = hijos.length
      ? hijos.map(h => `<div>${h.producto} <span class="cell-muted mono">${h.din.split('-')[1]}</span></div>`).join('')
      : `<span class="cell-muted">${u.producto === 'Sangre Total' ? 'Sin fraccionar' : u.producto}</span>`;
    return `<tr>
      <td class="mono">${u.din}</td>
      <td class="cell-strong">${nombreDonante(u.donante) || '—'}</td>
      <td class="mono">${u.donante}</td>
      <td class="mono">${u.abo}</td>
      <td>${tamizaje}</td>
      <td>${obtenidos}</td>
    </tr>`;
  }).join('') : `<tr><td colspan="6" class="cell-muted">No hay donaciones que coincidan.</td></tr>`;
  const total = document.getElementById('donaciones-count');
  if(total) total.textContent = `${filas.length} de ${bolsas.length} donaciones registradas`;
}

/* ---------------- Exportacion del inventario a CSV ----------------
   Exporta exactamente las unidades que se estan viendo (respeta el buscador
   y el filtro por estado). Se abre una ventana con la vista previa del
   archivo y dos acciones: descargarlo o copiarlo al portapapeles. */
function filasInventarioVisibles(){
  const q = (document.getElementById('inv-search')?.value || '').trim().toLowerCase();
  const filtro = document.getElementById('inv-filter')?.value || '';
  return state.inventory
    .filter(u => !filtro || u.status === filtro)
    .filter(u => !q || u.din.toLowerCase().includes(q) || u.abo.toLowerCase().includes(q));
}

function inventarioComoCSV(filas){
  const cabecera = ['DIN (ISBT 128)', 'Hemocomponente', 'ABO/Rh', 'Camara', 'Vence', 'Estado'];
  const cuerpo = filas.map(u => [u.din, u.producto, u.abo, u.camara, u.exp, statusMeta[u.status][1]]);
  // Separador ";" y comillas dobles: es lo que abre bien Excel en configuracion regional peruana.
  return [cabecera, ...cuerpo]
    .map(fila => fila.map(celda => `"${String(celda).replace(/"/g, '""')}"`).join(';'))
    .join('\r\n');
}

function exportarInventario(){
  const filas = filasInventarioVisibles();
  if(!filas.length){ toast('No hay unidades que exportar con el filtro actual', true); return; }
  const csv = inventarioComoCSV(filas);
  const vistaPrevia = csv.split('\r\n').slice(0, 6).join('\n');
  const resto = filas.length > 5 ? `\n… y ${filas.length - 5} unidades más` : '';
  window.csvInventario = csv;
  openModal(`
    <div class="modal-head">
      <div><div class="modal-title">Exportar inventario</div>
      <div class="card-desc">${filas.length} ${filas.length === 1 ? 'unidad' : 'unidades'} · archivo CSV compatible con Excel</div></div>
      <button class="modal-close" onclick="closeModal()">✕</button>
    </div>
    <pre class="csv-preview">${vistaPrevia.replace(/</g, '&lt;')}${resto}</pre>
    <div style="display:flex; gap:8px; flex-wrap:wrap; margin-top:14px;">
      <button class="btn btn-primary btn-sm" onclick="descargarInventarioCSV()">Descargar CSV</button>
      <button class="btn btn-ghost btn-sm" onclick="copiarInventarioCSV()">Copiar al portapapeles</button>
    </div>
  `);
}

function nombreArchivoInventario(){
  const d = new Date();
  const p = n => String(n).padStart(2, '0');
  return `inventario-hlev-${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}.csv`;
}

function descargarInventarioCSV(){
  const nombre = nombreArchivoInventario();
  // El "﻿" es la marca que necesita Excel para respetar las tildes.
  const blob = new Blob(['﻿' + window.csvInventario], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombre;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  logAudit('Exportó el inventario a CSV', nombre);
  toast(`Inventario exportado: ${nombre}`);
  closeModal();
}

function copiarInventarioCSV(){
  const texto = window.csvInventario;
  const listo = () => { logAudit('Copió el inventario en formato CSV', `${texto.split('\r\n').length - 1} unidades`);
                        toast('Inventario copiado al portapapeles'); closeModal(); };
  if(navigator.clipboard && navigator.clipboard.writeText){
    navigator.clipboard.writeText(texto).then(listo).catch(() => seleccionarVistaPrevia());
  } else {
    seleccionarVistaPrevia();
  }
}

function seleccionarVistaPrevia(){
  const pre = document.querySelector('.csv-preview');
  if(!pre){ toast('No se pudo copiar automáticamente', true); return; }
  const rango = document.createRange();
  rango.selectNodeContents(pre);
  const sel = window.getSelection();
  sel.removeAllRanges();
  sel.addRange(rango);
  toast('Texto seleccionado: copia con Ctrl+C', true);
}

function verUnidad(din){
  const u = state.inventory.find(x=>x.din===din);
  if(!u) return;
  const [color,label] = statusMeta[u.status];
  openModal(`
    <div class="modal-head">
      <div><div class="modal-title">Unidad ${u.din}</div><div class="card-desc">${u.producto} · ${u.abo}</div></div>
      <button class="modal-close" onclick="closeModal()">✕</button>
    </div>
    <div style="display:flex; flex-direction:column; gap:10px; font-size:13px;">
      <div class="match-row"><span>Cámara</span><strong>${u.camara}</strong></div>
      <div class="match-row"><span>Vencimiento</span><strong>${u.exp}</strong></div>
      <div class="match-row"><span>Estado actual</span>${pill(color,label)}</div>
    </div>
    <div class="divider" style="margin:16px 0;"></div>
    <div style="display:flex; gap:8px; flex-wrap:wrap;">
      <button class="btn btn-ghost btn-sm" onclick="cambiarEstadoUnidad('${u.din}','reservado')">Reservar</button>
      <button class="btn btn-ghost btn-sm" onclick="cambiarEstadoUnidad('${u.din}','disponible')">Liberar a disponible</button>
      <button class="btn btn-ghost btn-sm" onclick="cambiarEstadoUnidad('${u.din}','bloqueado')">Bloquear</button>
      <button class="btn btn-ghost btn-sm" onclick="descartarUnidad('${u.din}')">Descartar</button>
      <button class="btn btn-ghost btn-sm" onclick="verTrazabilidad('${u.din}')">Ver trazabilidad</button>
    </div>
  `);
}

/* El descarte pide el motivo en una ventana propia del sistema: los cuadros
   nativos del navegador (prompt) estan bloqueados en muchos navegadores y
   dejaban el boton sin efecto. */
function descartarUnidad(din){
  const u = state.inventory.find(x=>x.din===din);
  if(!u) return;
  openModal(`
    <div class="modal-head">
      <div><div class="modal-title">Descartar unidad</div><div class="card-desc">DIN ${u.din} · ${u.producto}</div></div>
      <button class="modal-close" onclick="closeModal()">✕</button>
    </div>
    <div class="field" style="margin-bottom:14px;">
      <label>Motivo del descarte</label>
      <select class="input" id="descarte-motivo">
        <option>Caducidad</option>
        <option>Rotura de la bolsa</option>
        <option>Contaminación</option>
        <option>Ruptura de la cadena de frío</option>
        <option>Reactividad serológica confirmada</option>
      </select>
    </div>
    <div class="checkline" style="margin-bottom:14px;">
      <input type="checkbox" id="descarte-evidencia" checked>
      <label for="descarte-evidencia" style="font-weight:400;">Se adjuntó evidencia fotográfica del descarte</label>
    </div>
    <div style="display:flex; gap:8px; justify-content:flex-end;">
      <button class="btn btn-ghost btn-sm" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary btn-sm" onclick="confirmarDescarte('${u.din}')">Confirmar descarte</button>
    </div>
  `);
}

function confirmarDescarte(din){
  const u = state.inventory.find(x=>x.din===din);
  if(!u) return;
  const motivo = document.getElementById('descarte-motivo').value;
  const conEvidencia = document.getElementById('descarte-evidencia').checked;
  u.status = 'incinerado';
  logAudit(`Descartó unidad — motivo: ${motivo}${conEvidencia ? ' (con evidencia fotográfica adjunta)' : ''}`, `DIN ${din}`);
  toast(`Unidad ${din} descartada e incinerada`, true);
  closeModal();
  renderInventory();
}

function verTrazabilidad(din){
  const u = state.inventory.find(x=>x.din===din);
  if(!u) return;
  const raiz = u.din.split('-')[0];
  const don = state.donantes.find(x=>x.dni===u.donante);
  const sero = state.serologia.filter(x=>x.din===raiz);
  let tamizaje;
  if(sero.length){
    const malos = sero.filter(x=>x.resultado!=='No reactivo');
    tamizaje = malos.length
      ? [ 'var(--red-700)', 'Tamizaje serológico: no conforme', `${malos.map(x=>x.marcador+' — '+x.resultado).join(', ')} · ${sero[0].digitador} · ${sero[0].fecha}` ]
      : [ 'var(--green-700)', 'Tamizaje serológico: conforme', `${sero.length} marcadores no reactivos · ${sero[0].digitador} · ${sero[0].fecha}` ];
  } else if(u.status==='cuarentena' || u.status==='fraccionado'){
    tamizaje = [ 'var(--purple-700)', 'Tamizaje serológico pendiente', 'La unidad sigue en cuarentena hasta registrar los resultados' ];
  } else {
    tamizaje = [ 'var(--slate-500)', 'Tamizaje serológico', 'Realizado antes de este sistema (sin detalle registrado)' ];
  }
  const pasos = [
    [ 'var(--green-700)', 'Donación', don ? `${don.nombre} · DNI ${don.dni}` : 'Donante no registrado (unidad anterior a este sistema)' ],
    tamizaje,
  ];
  if(u.din.includes('-')) pasos.push([ 'var(--blue-700)', 'Fraccionamiento', `Componente obtenido de la bolsa ${raiz}` ]);
  pasos.push([ 'var(--amber-700)', 'Ingreso al inventario', u.camara ]);
  pasos.push([ 'var(--red-700)', `Estado actual: ${statusMeta[u.status][1]}`, `Vence ${u.exp}` ]);
  openModal(`
    <div class="modal-head">
      <div><div class="modal-title">Trazabilidad completa</div><div class="card-desc">DIN ${din} — ciclo de vida</div></div>
      <button class="modal-close" onclick="closeModal()">✕</button>
    </div>
    <div class="timeline">
      ${pasos.map(([color, titulo, detalle]) => `<div class="timeline-item"><div class="timeline-dot" style="background:${color};"></div><div><div class="timeline-title">${titulo}</div><div class="timeline-meta">${detalle}</div></div></div>`).join('')}
    </div>
  `);
}

function cambiarEstadoUnidad(din, nuevoEstado){
  const u = state.inventory.find(x=>x.din===din);
  if(!u) return;
  const liberar = nuevoEstado === 'disponible' || nuevoEstado === 'reservado';
  if(liberar && u.status === 'cuarentena'){
    toast('La unidad sigue en cuarentena: registra primero su tamizaje serológico (Donantes)', true); return;
  }
  const raiz = u.din.split('-')[0];
  if(liberar && u.status === 'bloqueado' && state.serologia.some(x=>x.din===raiz && x.resultado!=='No reactivo')){
    toast('Esta unidad fue bloqueada por su tamizaje serológico y no puede liberarse', true); return;
  }
  u.status = nuevoEstado;
  logAudit(`Cambió estado de unidad a "${statusMeta[nuevoEstado][1]}"`, `DIN ${din}`);
  toast(`Unidad ${din} → ${statusMeta[nuevoEstado][1]}`);
  closeModal();
  renderInventory();
}

/* Fraccionamiento (RF-06) */
function fillFractSelect(){
  const sel = document.getElementById('fract-din');
  if(!sel) return;
  const current = sel.value;
  const bolsas = state.inventory.filter(u=>u.producto==='Sangre Total' && (u.status==='disponible' || u.status==='cuarentena'));
  sel.innerHTML = bolsas.map(u=>`<option value="${u.din}">${u.din} · ${u.abo} · ${statusMeta[u.status][1]}</option>`).join('')
    || '<option value="">Sin bolsas de sangre total por fraccionar</option>';
  if(current && bolsas.some(u=>u.din===current)) sel.value = current;
}
function fraccionarUnidad(){
  const din = document.getElementById('fract-din').value;
  if(!din){ toast('No hay una bolsa de sangre total para fraccionar', true); return; }
  const matriz = state.inventory.find(u=>u.din===din);
  const children = [
    {suffix:"CH", producto:"Concentrado de Hematíes"},
    {suffix:"PFC", producto:"Plasma Fresco Congelado"},
    {suffix:"CRIO1", producto:"Crioprecipitado"},
    {suffix:"CP", producto:"Concentrado de Plaquetas"},
  ];
  // Los componentes salen con el mismo estado de la bolsa (si aún está en
  // cuarentena, siguen en cuarentena hasta que se registre el tamizaje) y
  // conservan el donante de origen.
  children.forEach(c=>{
    state.inventory.push({
      din: din + "-" + c.suffix, producto: c.producto, abo: matriz.abo,
      camara: matriz.camara, exp: matriz.exp, status: matriz.status, donante: matriz.donante,
    });
  });
  matriz.status = 'fraccionado';
  logAudit(`Fraccionó bolsa de sangre total en 4 hemocomponentes`, `DIN matriz ${din}`);
  toast(`Se generaron 4 hemocomponentes a partir de ${din}`);
  renderInventory();
}

/* ---------------- Donantes (RF-03, RF-04) ---------------- */
/* Un diferimiento temporal siempre lleva fecha de fin (campo diferimiento_hasta de la base). */
const SIN_ANTECEDENTES = /^(ninguno|ninguna|ningun|no|no refiere|sin antecedentes|n\/a|niega)\.?$/i;
function evaluarAntecedentes(texto){
  const t = String(texto || '').trim();
  if(!t || SIN_ANTECEDENTES.test(t)) return null;
  const n = t.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
  if(/(conducta de riesgo|riesgo|vih|sida|hepatitis|sifilis|drogas|inyectable)/.test(n))
    return { motivo: 'Antecedente de conducta de riesgo declarado', tipo: 'Permanente', dias: 0 };
  if(/(viaje|malaria|endemic|dengue|zika|chagas|selva)/.test(n))
    return { motivo: 'Antecedente epidemiológico declarado (viaje a zona endémica)', tipo: 'Temporal', dias: 365 };
  if(/(tatuaje|piercing|perforacion|cirugia|operacion|transfusion|vacuna|antibiotic|medicacion|medicamento|embarazo|parto)/.test(n))
    return { motivo: 'Antecedente médico declarado (tatuaje, cirugía, vacuna o medicación reciente)', tipo: 'Temporal', dias: 180 };
  // Cualquier otro antecedente escrito no se da por bueno: queda diferido hasta que el médico lo evalúe
  return { motivo: 'Antecedente declarado pendiente de evaluación médica', tipo: 'Temporal', dias: 30 };
}
/* Evalúa peso, hemoglobina y antecedentes. Devuelve null si es apto, o { motivo, tipo, dias }. */
function evaluarDonante(){
  const peso = parseFloat(document.getElementById('don-peso').value);
  const hb = parseFloat(document.getElementById('don-hb').value);
  if(!isNaN(peso) && peso < 50) return { motivo: `Peso bajo el mínimo para donar (${peso} kg < 50 kg)`, tipo: 'Temporal', dias: 30 };
  if(!isNaN(hb) && hb < 12.5) return { motivo: `Hemoglobina bajo el mínimo (${hb} g/dL < 12.5 g/dL)`, tipo: 'Temporal', dias: 30 };
  return evaluarAntecedentes(document.getElementById('don-antecedentes').value);
}
function evaluarAptitud(){
  const pill = document.getElementById('aptitud-pill');
  const ev = evaluarDonante();
  if(pill){
    if(ev){
      pill.className = 'pill ' + (ev.tipo === 'Permanente' ? 'red' : 'amber');
      pill.innerHTML = `<span class="dot"></span>Aptitud automática: ${ev.tipo === 'Permanente' ? 'EXCLUIDO' : 'DIFERIDO'} — ${ev.motivo}`;
    } else {
      pill.className = 'pill green'; pill.innerHTML = `<span class="dot"></span>Evaluación: APTO para donar`;
    }
  }
  return ev;
}

/* ---------------- Registro de donantes ---------------- */
function renderDonantes(){
  const body = document.getElementById('donantes-rows');
  if(!body) return;
  const q = (document.getElementById('don-buscar')?.value || '').trim().toLowerCase();
  const filas = state.donantes.filter(d =>
    !q || d.nombre.toLowerCase().includes(q) || d.dni.includes(q));
  body.innerHTML = filas.length ? filas.map(d => {
    const [color, etiqueta] = d.estado.split(':');
    return `<tr>
      <td class="cell-strong">${d.nombre}</td>
      <td class="mono">${d.dni}</td>
      <td class="mono">${d.abo}</td>
      <td class="cell-muted">${d.tipo}</td>
      <td class="mono">${d.ultimaDonacion}</td>
      <td class="mono">${d.donaciones}</td>
      <td>${pill(color, etiqueta)}</td>
      <td>${color === 'green' ? `<button class="btn btn-ghost btn-sm" onclick="abrirModalDonacion('${d.dni}')">Registrar donación</button>` : ''}</td>
    </tr>`;
  }).join('') : `<tr><td colspan="8" class="cell-muted">Ningún donante coincide con la búsqueda.</td></tr>`;
  const total = document.getElementById('don-count');
  if(total) total.textContent = `${filas.length} de ${state.donantes.length} donantes registrados`;
}

function registrarDonante(){
  if(!VAL.formulario([
    ['don-nombre','nombre',{label:'Nombres y apellidos'}],
    ['don-dni','dni',{label:'DNI'}],
    ['don-fecnac','fechaNacimiento',{label:'Fecha de nacimiento'}],
    ['don-peso','peso',{label:'Peso'}],
    ['don-hb','hemoglobina',{label:'Hemoglobina'}],
    ['don-telefono','telefono',{label:'Teléfono'}],
    ['don-correo','correoPersonal',{label:'Correo electrónico'}],
    ['don-sexo','seleccion',{label:'Sexo'}],
    ['don-direccion','texto',{label:'Dirección'}],
    ['don-antecedentes','texto',{label:'Antecedentes'}]
  ])) return;
  const nombre = document.getElementById('don-nombre').value.trim();
  const dni = document.getElementById('don-dni').value.trim();
  if(state.donantes.some(d => d.dni === dni)){ toast('Ya existe un donante registrado con ese DNI', true); return; }
  if(!document.getElementById('don-consentimiento').checked){
    toast('Falta registrar el consentimiento informado firmado', true);
    return;
  }
  const ev = evaluarAptitud();
  const motivo = ev ? ev.motivo : null;
  const donante = {
    nombre, dni,
    abo: document.getElementById('don-abo').value || 'Por confirmar',
    tipo: document.getElementById('don-tipo').selectedOptions[0].textContent,
    ultimaDonacion: '—',
    donaciones: 0,
    estado: !ev ? 'green:Apto' : ev.tipo === 'Permanente' ? 'red:Excluido' : 'amber:Diferido',
    fechaNacimiento: document.getElementById('don-fecnac').value.trim(),
    pesoKg: parseFloat(document.getElementById('don-peso').value) || null,
    antecedentes: document.getElementById('don-antecedentes').value.trim(),
    sexo: document.getElementById('don-sexo').value,
    telefono: document.getElementById('don-telefono').value.trim(),
    correo: document.getElementById('don-correo').value.trim(),
    direccion: document.getElementById('don-direccion').value.trim(),
    hemoglobina: parseFloat(document.getElementById('don-hb').value) || null,
    campana: document.getElementById('don-campana').selectedOptions[0].textContent.replace(/^—\s*Ninguna.*$/, 'Donación directa'),
    consentimiento: true,
    motivoDiferimiento: motivo || null,
    tipoDiferimiento: ev ? ev.tipo : null,
    vigenciaDiferimiento: ev && ev.tipo === 'Temporal' ? formatoFecha(sumarDias(new Date(), ev.dias)) : '—',
  };
  state.donantes.unshift(donante);
  renderDonantes();

  if(motivo){
    renderDiferimientos();
    logAudit('Registró donante — diferido automáticamente', nombre);
    toast(`${nombre} quedó ${ev.tipo === 'Permanente' ? 'excluido' : 'diferido'}: ${motivo}`, true);
  } else {
    logAudit('Registró la ficha de un donante — APTO', nombre);
    toast(`${nombre} quedó registrado como apto. Ya puede registrar su donación`);
  }
  // El formulario queda listo para el siguiente donante
  ['don-nombre','don-dni','don-fecnac','don-peso','don-hb','don-abo','don-antecedentes','don-sexo','don-telefono','don-correo','don-direccion'].forEach(id => {
    const campo = document.getElementById(id);
    if(campo) campo.value = '';
  });
  VAL.limpiar(['don-sexo','don-nombre','don-dni','don-fecnac','don-peso','don-hb','don-antecedentes','don-telefono','don-correo','don-direccion']);
  document.getElementById('don-consentimiento').checked = false;
  switchTab('donantes','donantes:registro');
}

/* ---------------- Donación: del donante a la unidad ---------------- */
const INTERVALO_DONACION_DIAS = 90;
const GRUPOS_ABO = ['O POS','O NEG','A POS','A NEG','B POS','B NEG','AB POS','AB NEG'];

function motivoNoDonar(d){
  const etiqueta = d.estado.split(':')[1];
  if(!d.estado.startsWith('green')) return `${d.nombre} está ${etiqueta.toLowerCase()} y no puede donar`;
  const ultima = parseFecha(d.ultimaDonacion);
  if(ultima){
    const dias = Math.floor((Date.now() - ultima.getTime()) / 86400000);
    if(dias >= 0 && dias < INTERVALO_DONACION_DIAS){
      return `${d.nombre} donó hace ${dias} días; podrá volver a donar desde el ${formatoFecha(sumarDias(ultima, INTERVALO_DONACION_DIAS))}`;
    }
  }
  return null;
}
function abrirModalDonacion(dni){
  const d = state.donantes.find(x => x.dni === dni);
  if(!d) return;
  const motivo = motivoNoDonar(d);
  if(motivo){ toast(motivo, true); return; }
  openModal(`
    <div class="modal-head"><div><div class="modal-title">Registrar donación</div>
    <div class="card-desc">${d.nombre} · DNI ${d.dni}</div></div>
    <button class="modal-close" onclick="closeModal()">✕</button></div>
    <div style="display:flex; flex-direction:column; gap:12px;">
      <div class="field"><label>Grupo sanguíneo confirmado en laboratorio</label>
        <select class="input" id="dn-grupo">
          ${GRUPOS_ABO.map(g => `<option ${g === d.abo ? 'selected' : ''}>${g}</option>`).join('')}
        </select>
      </div>
      <div class="cell-muted" style="font-size:12.5px;">
        Se generará una bolsa de <strong>sangre total</strong> con su código DIN. Entra al inventario
        <strong>en cuarentena</strong> y solo pasa a disponible cuando su tamizaje serológico sale conforme.
      </div>
    </div>
    <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:18px;">
      <button class="btn btn-ghost btn-sm" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary btn-sm" onclick="confirmarDonacion('${d.dni}')">Registrar donación</button>
    </div>`);
}
function confirmarDonacion(dni){
  const d = state.donantes.find(x => x.dni === dni);
  if(!d) return;
  const motivo = motivoNoDonar(d);
  if(motivo){ toast(motivo, true); return; }
  const grupo = document.getElementById('dn-grupo').value;
  const din = siguienteDIN();
  const hoy = new Date();
  state.inventory.push({
    din, producto: 'Sangre Total', abo: grupo, camara: 'Cámara 1 · Refrig.',
    exp: formatoFecha(sumarDias(hoy, 35)), status: 'cuarentena', donante: d.dni,
  });
  d.donaciones += 1;
  d.ultimaDonacion = formatoFecha(hoy);
  d.abo = grupo;
  logAudit('Registró una donación — unidad en cuarentena', `DIN ${din} · ${d.nombre}`);
  toast(`Donación registrada: unidad ${din} en cuarentena, pendiente de tamizaje`);
  closeModal();
  renderDonantes();
  fillSerologiaSelect();
}

function renderDiferimientos(){
  const tabla = document.getElementById('diferimiento-rows');
  if(!tabla) return;
  // Los diferidos se calculan de los propios donantes: un solo dato, sin lista aparte
  const diferidos = state.donantes.filter(d => !d.estado.startsWith('green'))
    .map(d => [d.nombre, d.motivoDiferimiento || 'Sin motivo registrado',
               d.estado.startsWith('red') ? 'red' : 'amber',
               d.tipoDiferimiento || (d.estado.startsWith('red') ? 'Permanente' : 'Temporal'),
               d.vigenciaDiferimiento || '—']);
  tabla.innerHTML = diferidos.map(([nombre,motivo,color,tipo,vigencia])=>{
    const tipoColor = tipo==='Permanente' ? 'red' : 'amber';
    const estado = tipo==='Permanente' ? pill('red','Excluido') : pill('amber','Diferido');
    return `<tr><td class="cell-strong">${nombre}</td><td class="cell-muted">${motivo}</td><td>${pill(tipoColor,tipo)}</td><td class="mono">${vigencia}</td><td>${estado}</td></tr>`;
  }).join('');
}

/* ---------------- Serología (RF-07, RF-08) ----------------
   Cada marcador se digita dos veces por separado (Digitación 1 y 2).
   Solo se acepta el resultado si las dos coinciden; así un error de tipeo no
   deja pasar una unidad contaminada. Un resultado reactivo o indeterminado
   bloquea la bolsa (y todos sus hemocomponentes) y difiere al donante. */
function unidadesPorTamizar(){
  return state.inventory.filter(u => !u.din.includes('-') && (u.status === 'cuarentena' ||
    (u.status === 'fraccionado' && state.inventory.some(h => h.din.startsWith(u.din + '-') && h.status === 'cuarentena'))));
}
function fillSerologiaSelect(){
  const sel = document.getElementById('serologia-din');
  if(!sel) return;
  const current = sel.value;
  const pendientes = unidadesPorTamizar();
  sel.innerHTML = pendientes.map(u=>`<option value="${u.din}">${u.din} · ${u.producto} · ${nombreDonante(u.donante) || 'donante no registrado'}</option>`).join('')
    || '<option value="">Sin unidades en cuarentena</option>';
  if(current && pendientes.some(u=>u.din===current)) sel.value = current;
  renderSerologia();
  renderTamizajes();
}
const OPCIONES_SERO = '<option value="">Seleccionar…</option><option>No reactivo</option><option>Reactivo</option><option>Indeterminado</option>';
function renderSerologia(){
  const din = document.getElementById('serologia-din')?.value;
  const body = document.getElementById('serologia-rows');
  if(!body) return;
  if(!din){ body.innerHTML = `<tr><td colspan="4" class="cell-muted">No hay unidades pendientes de tamizaje.</td></tr>`; return; }
  body.innerHTML = serologyMarkers.map((m,i)=>`
    <tr>
      <td class="cell-strong">${m}</td>
      <td><select class="select-inline" id="sero-1-${i}" onchange="actualizarEstadoSerologia(${i})">${OPCIONES_SERO}</select></td>
      <td><select class="select-inline" id="sero-2-${i}" onchange="actualizarEstadoSerologia(${i})">${OPCIONES_SERO}</select></td>
      <td id="sero-estado-${i}">${pill('slate','Pendiente')}</td>
    </tr>`).join('');
  document.getElementById('serologia-alert').innerHTML = '';
}
function actualizarEstadoSerologia(i){
  const v1 = document.getElementById(`sero-1-${i}`).value;
  const v2 = document.getElementById(`sero-2-${i}`).value;
  document.getElementById(`sero-estado-${i}`).innerHTML =
    !v1 || !v2 ? pill('slate','Pendiente') : v1 === v2 ? pill('green','Coinciden') : pill('red','No coinciden');
}
function renderTamizajes(){
  const body = document.getElementById('tamizajes-rows');
  if(!body) return;
  const grupos = [];
  state.serologia.forEach(r => {
    let g = grupos.find(x => x.din === r.din);
    if(!g){ g = { din: r.din, filas: [] }; grupos.push(g); }
    g.filas.push(r);
  });
  body.innerHTML = grupos.length ? grupos.reverse().map(g => {
    const unidad = state.inventory.find(u => u.din === g.din);
    const malos = g.filas.filter(r => r.resultado !== 'No reactivo');
    const resultado = malos.length
      ? pill('red', malos.map(r => `${r.marcador}: ${r.resultado}`).join(', '))
      : pill('green', 'Conforme — todos no reactivos');
    return `<tr><td class="mono">${g.din}</td><td class="cell-muted">${nombreDonante(unidad?.donante) || '—'}</td>
      <td>${resultado}</td><td class="cell-muted">${g.filas[0].digitador}<div>${g.filas[0].fecha}</div></td></tr>`;
  }).join('') : `<tr><td colspan="4" class="cell-muted">Todavía no se registran tamizajes.</td></tr>`;
}
function guardarSerologia(){
  const din = document.getElementById('serologia-din')?.value;
  if(!din){ toast('Selecciona una unidad para registrar el tamizaje', true); return; }
  const lecturas = serologyMarkers.map((m,i) => ({
    m, d1: document.getElementById(`sero-1-${i}`).value, d2: document.getElementById(`sero-2-${i}`).value,
  }));
  if(lecturas.some(l => !l.d1 || !l.d2)){
    toast('Completa las dos digitaciones de todos los marcadores', true); return;
  }
  const alerta = document.getElementById('serologia-alert');
  const difieren = lecturas.filter(l => l.d1 !== l.d2);
  if(difieren.length){
    const lista = difieren.map(l => l.m).join(', ');
    logAudit('Detectó discrepancia en la doble digitación serológica', `DIN ${din} · ${lista}`);
    toast('Las dos digitaciones no coinciden: repite la lectura', true);
    alerta.innerHTML = `
      <div style="margin-top:14px; padding:12px 14px; background:var(--amber-50); border-radius:10px; font-size:12.5px; color:var(--amber-700);">
        <strong>No se guardó el tamizaje.</strong> Las dos digitaciones no coinciden en: ${lista}. Repite la lectura de esos marcadores y vuelve a digitarlos.
      </div>`;
    return;
  }
  const por = `${state.currentUser} (${state.currentRole})`;
  const fecha = nowStr().slice(0, 16);
  state.serologia = state.serologia.filter(r => r.din !== din);
  lecturas.forEach(l => state.serologia.push({ din, marcador: l.m, dig1: l.d1, dig2: l.d2, resultado: l.d1, digitador: por, fecha }));

  const reactivos = lecturas.filter(l => l.d1 === 'Reactivo');
  const indeterminados = lecturas.filter(l => l.d1 === 'Indeterminado');
  const afectadas = state.inventory.filter(u => u.din === din || u.din.startsWith(din + '-'));
  const raiz = state.inventory.find(u => u.din === din);
  let mensajeAlerta = '';

  if(reactivos.length || indeterminados.length){
    const lista = [...reactivos, ...indeterminados].map(l => l.m).join(', ');
    afectadas.forEach(u => { if(u.status !== 'fraccionado') u.status = 'bloqueado'; });
    const donante = state.donantes.find(d => d.dni === raiz?.donante);
    if(donante){
      if(reactivos.length){
        donante.estado = 'red:Excluido';
        donante.motivoDiferimiento = `Tamizaje reactivo a ${lista} (DIN ${din}) — derivar a prueba confirmatoria`;
        donante.tipoDiferimiento = 'Permanente'; donante.vigenciaDiferimiento = '—';
      } else {
        donante.estado = 'amber:Diferido';
        donante.motivoDiferimiento = `Tamizaje indeterminado en ${lista} (DIN ${din}) — repetir la muestra`;
        donante.tipoDiferimiento = 'Temporal'; donante.vigenciaDiferimiento = formatoFecha(sumarDias(new Date(), 30));
      }
    }
    logAudit(`Bloqueó unidad automáticamente por resultado no conforme en ${lista}`, `DIN ${din}`);
    pushAlert('red', 'Unidad bloqueada por tamizaje serológico', `DIN ${din} · ${lista}`);
    toast(`Unidad ${din} bloqueada automáticamente (${lista})`, true);
    mensajeAlerta = `
      <div style="margin-top:14px; padding:12px 14px; background:var(--red-50); border-radius:10px; font-size:12.5px; color:var(--red-700);">
        <strong>Bloqueo automático:</strong> la unidad DIN ${din}${afectadas.length > 1 ? ' y sus hemocomponentes' : ''} quedó bloqueada por ${lista}. No puede distribuirse ni despacharse.${donante ? ` El donante ${donante.nombre} quedó ${reactivos.length ? 'excluido hasta la prueba confirmatoria' : 'diferido'}.` : ''}
      </div>`;
  } else {
    afectadas.forEach(u => { if(u.status === 'cuarentena') u.status = 'disponible'; });
    logAudit('Registró tamizaje serológico conforme — unidad liberada', `DIN ${din}`);
    toast(`Unidad ${din} conforme — liberada a inventario disponible`);
  }
  renderInventory();
  renderDonantes();
  renderDiferimientos();
  fillSerologiaSelect();
  alerta.innerHTML = mensajeAlerta;
}

/* ---------------- Etiquetado ISBT 128 (RF-05) ---------------- */
function updateEtiquetaPreview(){
  const abo = document.getElementById('et-abo').value;
  const [code, nombre] = document.getElementById('et-producto').value.split('|');
  document.getElementById('et-code').value = code;
  document.getElementById('label-abo').textContent = abo;
  document.getElementById('label-code').textContent = code;
}
function generarNuevoDIN(){
  const din = generarDIN();
  const now = new Date();
  const p = n=>String(n).padStart(2,'0');
  const colecta = `${p(now.getDate())}/${p(now.getMonth()+1)}/${now.getFullYear()} ${p(now.getHours())}:${p(now.getMinutes())}`;
  const vence = new Date(now.getTime() + 30*24*3600*1000);
  const venceStr = `${p(vence.getDate())}/${p(vence.getMonth()+1)}/${vence.getFullYear()}`;

  document.getElementById('et-din').value = "=" + din;
  document.getElementById('et-colecta').value = colecta;
  document.getElementById('et-vence').value = venceStr;
  document.getElementById('label-din-text').textContent = "=" + din;
  document.getElementById('label-colecta').textContent = `${p(now.getDate())}/${p(now.getMonth()+1)}/${String(now.getFullYear()).slice(2)}`;
  document.getElementById('label-vence').textContent = `${p(vence.getDate())}/${p(vence.getMonth()+1)}/${String(vence.getFullYear()).slice(2)}`;
  updateEtiquetaPreview();

  const abo = document.getElementById('et-abo').value;
  const [code, nombre] = document.getElementById('et-producto').value.split('|');
  state.inventory.unshift({din, producto: nombre, abo, camara:"Recepción · Sin asignar", exp: venceStr, status:"cuarentena"});
  logAudit('Generó el código de una nueva donación', `DIN ${din}`);
  toast(`Nuevo DIN generado: ${din}`);
  renderInventory();
}
function imprimirEtiqueta(){
  logAudit('Imprimió etiqueta ISBT 128', document.getElementById('et-din').value);
  toast('Enviando etiqueta a la impresora térmica…');
}
function leerCodigo(){
  toast('Escáner listo — apunte al código de barras 2D');
}
function generarCertificado(){
  const din = document.getElementById('et-din').value;
  logAudit('Generó certificado de calidad con código QR de verificación', din);
  openModal(`
    <div class="modal-head">
      <div><div class="modal-title">Certificado de calidad</div><div class="card-desc">${din}</div></div>
      <button class="modal-close" onclick="closeModal()">✕</button>
    </div>
    <div style="display:flex; flex-direction:column; align-items:center; gap:10px; padding:10px 0;">
      <svg width="140" height="140" viewBox="0 0 140 140">
        <rect width="140" height="140" fill="#fff" stroke="#12161F" stroke-width="2"/>
        ${Array.from({length:9}).map((_,r)=>Array.from({length:9}).map((_,c)=>((r*7+c*13)%3===0)?`<rect x="${8+c*14}" y="${8+r*14}" width="12" height="12" fill="#12161F"/>`:'').join('')).join('')}
      </svg>
      <div class="cell-muted" style="text-align:center;">Escanee para verificar que la unidad es auténtica</div>
    </div>
  `);
  toast('Certificado de calidad generado con código QR');
}

/* ---------------- Solicitudes (RF-11, RF-12) ---------------- */
/* La fecha solo se pide cuando hace falta: si es una reserva para cirugía
   o si la prioridad es "Programada". Con Urgente o Diferible no aparece. */
function requiereFechaSolicitud(){
  return document.getElementById('sol-tipo').value === 'reserva'
      || document.getElementById('sol-prioridad').value === 'Programada';
}
function toggleTipoSolicitud(){
  const pide = requiereFechaSolicitud();
  const esReserva = document.getElementById('sol-tipo').value === 'reserva';
  document.getElementById('sol-reserva-field').style.display = pide ? '' : 'none';
  document.getElementById('sol-reserva-label').textContent = esReserva ? 'Fecha límite de la reserva' : 'Fecha programada de la transfusión';
  if(!pide){
    const f = document.getElementById('sol-reserva-fecha');
    f.value = '';
    VAL.limpiar(['sol-reserva-fecha']);
  }
}
/* Al completar la historia clinica, si el paciente ya tiene una solicitud previa
   se rellenan solos su nombre y su DNI (y quedan bloqueados para no desalinear datos). */
function autocompletarPaciente(){
  const hc = (document.getElementById('sol-hc').value || '').trim().toUpperCase();
  const dni = document.getElementById('sol-dni');
  const nom = document.getElementById('sol-paciente');
  if(!dni || !nom) return;
  const previa = hc.length === 13
    ? (state.pacientes || []).find(p => String(p.hc).toUpperCase() === hc && p.dni)
    : null;
  if(previa){
    dni.value = previa.dni; nom.value = previa.nombre;
    dni.readOnly = true; nom.readOnly = true;
    VAL.limpiar(['sol-dni','sol-paciente']);
  } else if(dni.readOnly){
    dni.readOnly = false; nom.readOnly = false;
    dni.value = ''; nom.value = '';
  }
}
/* (Ya no se usa al enviar una solicitud: los pacientes se registran en su módulo.) */
function recordarPaciente(hc, dni, nombre){
  state.pacientes = (state.pacientes || []).filter(p => String(p.hc).toUpperCase() !== hc.toUpperCase());
  state.pacientes.push({ hc: hc.toUpperCase(), dni, nombre });
  const lista = document.getElementById('lista-pacientes');
  if(lista && !lista.querySelector(`option[value="${hc.toUpperCase()}"]`)){
    const op = document.createElement('option');
    op.value = hc.toUpperCase(); op.label = `${nombre} · DNI ${dni}`;
    lista.appendChild(op);
  }
}
function importarHIS(){
  const pac = (state.pacientes || [])[0];
  if(!pac){ toast('No hay pacientes registrados: registra primero al paciente en el módulo Pacientes', true); return; }
  document.getElementById('sol-hc').value = pac.hc;
  autocompletarPaciente();
  document.getElementById('sol-cie10').value = 'K92.2 — Hemorragia digestiva alta';
  document.getElementById('sol-componente').value = '2 unidades — Concentrado de Hematíes';
  VAL.limpiar(['sol-paciente','sol-dni','sol-hc','sol-cie10','sol-componente']);
  toast('Orden importada automáticamente desde el HIS/SIS hospitalario');
}
function enviarSolicitud(){
  const tipo = document.getElementById('sol-tipo').value;
  const pideFecha = requiereFechaSolicitud();
  if(!VAL.formulario([
    ['sol-paciente','nombre',{label:'Paciente'}],
    ['sol-hc','hc',{label:'Historia clínica'}],
    ['sol-dni','dni',{label:'DNI del paciente'}],
    ['sol-cie10','cie10',{label:'Diagnóstico CIE-10'}],
    ['sol-componente','componente',{label:'Hemocomponente'}],
    ['sol-reserva-fecha','fechaFutura',{label: tipo==='reserva' ? 'Fecha límite de la reserva' : 'Fecha programada', req:pideFecha}]
  ])) return;
  const paciente = document.getElementById('sol-paciente').value.trim();
  const hcEscrita = document.getElementById('sol-hc').value.trim().toUpperCase();
  if(!(state.pacientes || []).some(p => String(p.hc).toUpperCase() === hcEscrita)){
    VAL.marcar(document.getElementById('sol-hc'), 'Esta historia clínica no está registrada');
    toast('Registra primero al paciente en el módulo Pacientes (todos sus datos son obligatorios)', true);
    return;
  }
  const req = {
    id: siguienteIdSolicitud(),
    paciente,
    dni: document.getElementById('sol-dni').value.trim(),
    hc: document.getElementById('sol-hc').value.trim().toUpperCase(),
    servicio: document.getElementById('sol-servicio').value,
    cie10: document.getElementById('sol-cie10').value.trim(),
    componente: document.getElementById('sol-componente').value.trim(),
    prioridad: document.getElementById('sol-prioridad').value,
    tipo, reservaFecha: pideFecha ? document.getElementById('sol-reserva-fecha').value.trim() : null,
    estado: 'pendiente',
  };
  state.solicitudes.unshift(req);
  if(req.dni) recordarPaciente(req.hc, req.dni, paciente);
  logAudit(tipo==='reserva' ? `Registró reserva quirúrgica programada` : pideFecha ? `Registró solicitud transfusional programada` : `Registró solicitud transfusional`, `${req.id} · ${req.servicio}`);
  toast(`Solicitud ${req.id} enviada al Banco de Sangre`);
  pushAlert('blue', `Nueva solicitud — ${req.servicio}`, `${req.componente} · ${req.cie10} · ${paciente}`);
  document.getElementById('sol-paciente').value=''; document.getElementById('sol-hc').value='';
  const d = document.getElementById('sol-dni'); d.value=''; d.readOnly=false;
  document.getElementById('sol-paciente').readOnly=false;
  document.getElementById('sol-cie10').value=''; document.getElementById('sol-componente').value='';
  document.getElementById('sol-reserva-fecha').value='';
  toggleTipoSolicitud();
  VAL.limpiar(['sol-paciente','sol-dni','sol-hc','sol-cie10','sol-componente','sol-reserva-fecha']);
  renderSolicitudes();
}
function siguienteIdSolicitud(){
  // Continúa la numeración a partir de la solicitud más alta que exista
  const mayor = state.solicitudes.reduce((m, r) => {
    const n = parseInt(String(r.id).split('-').pop(), 10);
    return isNaN(n) ? m : Math.max(m, n);
  }, state.reqCounter);
  return 'SOL-2026-' + String(mayor + 1).padStart(4, '0');
}
const ESTADOS_SOLICITUD = {
  pendiente:['amber','Pendiente'], compatible:['green','Compatible'], incompatible:['red','Incompatible']
};
function puedeProbarCompatibilidad(){ return state.currentRole !== 'Médico Solicitante'; }
function renderSolicitudes(){
  const body = document.getElementById('solicitudes-rows');
  if(!body) return;
  const puede = puedeProbarCompatibilidad();
  const tabCompat = document.querySelector('.tab[data-tab="sol:compat"]');
  if(tabCompat) tabCompat.style.display = puede ? '' : 'none';
  body.innerHTML = state.solicitudes.length ? state.solicitudes.map(r=>{
    const prColor = r.prioridad.startsWith('Urgente') ? 'red' : r.prioridad==='Programada' ? 'blue' : 'slate';
    const [colE, txtE] = ESTADOS_SOLICITUD[r.estado] || ESTADOS_SOLICITUD.pendiente;
    const tipoTag = r.reservaFecha ? `<div class="cell-muted">${r.tipo==='reserva' ? 'Reserva hasta' : 'Programada para el'} ${r.reservaFecha}</div>` : '';
    const accion = puede && r.estado!=='compatible'
      ? `<button class="btn btn-ghost btn-sm" onclick="validarSolicitud('${r.id}')">${r.estado==='incompatible' ? 'Repetir pruebas' : 'Validar'}</button>` : '';
    return `<tr>
      <td class="cell-strong">${r.paciente}${tipoTag}</td><td class="cell-muted">${r.servicio}</td><td class="mono">${r.cie10}</td>
      <td>${pill(prColor, r.prioridad)}</td><td>${pill(colE, txtE)}</td><td>${accion}</td>
    </tr>`;
  }).join('') : `<tr><td colspan="6" class="cell-muted">Aún no se registran solicitudes en esta sesión.</td></tr>`;
  fillCompatSelect();
}
function validarSolicitud(id){
  switchTab('sol','sol:compat');
  const sel = document.getElementById('compat-solicitud');
  if(sel){ sel.value = id; renderPruebas(); }
}
function fillCompatSelect(){
  const sel = document.getElementById('compat-solicitud');
  if(!sel) return;
  const previo = sel.value;
  const abiertas = state.solicitudes.filter(r=>r.estado!=='compatible');
  sel.innerHTML = abiertas.length ? abiertas.map(r=>`<option value="${r.id}">${r.id} · ${r.paciente}</option>`).join('')
    : '<option value="">No hay solicitudes por validar</option>';
  if(previo && abiertas.some(r=>r.id===previo)) sel.value = previo;
  const uni = document.getElementById('compat-unidad');
  if(uni){
    const libres = state.inventory.filter(u=>u.status==='disponible' || u.status==='reservado');
    const uPrevia = uni.value;
    uni.innerHTML = libres.length ? libres.map(u=>`<option value="${u.din}">${u.din} · ${u.producto} · ${u.abo}</option>`).join('')
      : '<option value="">No hay unidades disponibles</option>';
    if(uPrevia && libres.some(u=>u.din===uPrevia)) uni.value = uPrevia;
  }
  renderPruebas();
}
function renderPruebas(){
  const body = document.getElementById('pruebas-rows');
  if(!body) return;
  const id = document.getElementById('compat-solicitud').value;
  const filas = state.pruebas.filter(p=>p.solicitudId===id);
  body.innerHTML = filas.length ? filas.map(p=>{
    const [col, txt] = p.estado.split(':');
    return `<tr><td class="cell-strong">${p.prueba}</td><td class="mono">${p.muestra}</td><td class="mono">${p.unidad}</td>
      <td class="mono">${p.resultado}</td><td class="cell-muted">${p.validadoPor}<div>${p.fecha}</div></td><td>${pill(col, txt)}</td></tr>`;
  }).join('') : `<tr><td colspan="6" class="cell-muted">${id ? 'Esta solicitud todavía no tiene pruebas registradas.' : 'Selecciona una solicitud.'}</td></tr>`;
}
/* Compatibilidad ABO/Rh entre el paciente y la unidad. Hematíes: el paciente
   debe poder recibir los antígenos de la unidad. Plasma: se invierte la regla
   ABO y el Rh no se considera. */
function abo_rh(txt){
  const partes = txt.trim().split(' ');
  return { abo: partes[0], rh: partes[1] === 'POS' ? '+' : '-' };
}
function compatibleAboRh(paciente, unidad, producto){
  const p = abo_rh(paciente), u = abo_rh(unidad);
  const esPlasma = /plasma|crioprecipitado/i.test(producto);
  if(esPlasma){
    // Plasma donante AB sirve a todos; A a receptores A u O; B a B u O; O solo a O
    const tablaPlasma = { AB:['AB','A','B','O'], A:['A','O'], B:['B','O'], O:['O'] };
    return tablaPlasma[u.abo].includes(p.abo);
  }
  const tabla = { O:['O','A','B','AB'], A:['A','AB'], B:['B','AB'], AB:['AB'] };
  const aboOk = tabla[u.abo].includes(p.abo);
  const rhOk = !(u.rh === '+' && p.rh === '-');
  return aboOk && rhOk;
}
function validarCompatibilidad(){
  const id = document.getElementById('compat-solicitud').value;
  const req = state.solicitudes.find(r=>r.id===id);
  if(!req){ toast('Selecciona una solicitud a validar', true); return; }
  const din = document.getElementById('compat-unidad').value;
  const unidad = state.inventory.find(u=>u.din===din);
  if(!unidad){ toast('Selecciona la unidad a cruzar', true); return; }
  if(!VAL.formulario([['compat-muestra','muestra',{label:'Muestra del paciente'}]])) return;
  const muestra = document.getElementById('compat-muestra').value.trim();
  const grupo = document.getElementById('compat-grupo').value;
  const mayor = document.getElementById('compat-mayor').value;
  const menor = document.getElementById('compat-menor').value;
  const rai   = document.getElementById('compat-rai').value;

  // Antes de cruzar sangre se verifica el grupo: una unidad de grupo
  // incompatible se rechaza y no se registra ninguna prueba.
  if(!compatibleAboRh(grupo, unidad.abo, unidad.producto)){
    logAudit('Rechazó unidad por incompatibilidad ABO/Rh', `${req.id} · ${din}`);
    toast(`La unidad ${unidad.abo} no es compatible con un paciente ${grupo}. Elige otra unidad`, true);
    pushAlert('red', `Incompatibilidad ABO/Rh — ${req.id}`, `Unidad ${din} (${unidad.abo}) no es compatible con ${grupo}`);
    return;
  }
  const resultados = [
    ['Prueba mayor', mayor, mayor === 'Sin aglutinación'],
    ['Prueba menor', menor, menor === 'Sin aglutinación'],
    ['Rastreo de anticuerpos (RAI)', rai, rai === 'Negativo'],
  ];
  const todoOk = resultados.every(r=>r[2]);
  const por = `${state.currentUser} (${state.currentRole})`;
  const fecha = nowStr().slice(0, 16);
  // Las pruebas anteriores de esta solicitud se reemplazan por las nuevas
  state.pruebas = state.pruebas.filter(p=>p.solicitudId!==req.id);
  resultados.forEach(([prueba, resultado, ok])=>{
    const etiqueta = prueba.startsWith('Rastreo') ? (ok ? 'Negativo' : 'Positivo') : (ok ? 'Compatible' : 'Incompatible');
    state.pruebas.push({ solicitudId:req.id, prueba, muestra, unidad:din, grupoPaciente:grupo, resultado,
      estado:`${ok ? 'green' : 'red'}:${etiqueta}`, validadoPor:por, fecha });
  });
  req.estado = todoOk ? 'compatible' : 'incompatible';
  if(todoOk && unidad.status === 'disponible') unidad.status = 'reservado';
  logAudit(todoOk ? 'Validó compatibilidad cruzada — mayor, menor y RAI'
                  : 'Registró pruebas de compatibilidad con resultado incompatible', `${req.id} · ${din}`);
  if(todoOk){
    toast(`Solicitud ${req.id} compatible: unidad ${din} reservada para el paciente`);
  } else {
    toast(`Solicitud ${req.id} incompatible: revisa las pruebas y elige otra unidad`, true);
    pushAlert('red', `Pruebas incompatibles — ${req.id}`, `Unidad ${din} rechazada para ${req.paciente}`);
  }
  document.getElementById('compat-muestra').value = '';
  renderSolicitudes();
  renderPruebas();
}
function switchTab(group, tabKey){
  const tabBtn = [...document.querySelectorAll('.tab')].find(t=>t.dataset.tab===tabKey);
  if(tabBtn) tabBtn.click();
}

/* ---------------- Despacho (RF-13) ---------------- */
function fillDespachoContext(){ /* placeholder para futura sincronización con solicitudes */ }
function escanearHemocomponente(){
  const candidatos = state.inventory.filter(u=>u.status==='disponible' || u.status==='reservado');
  if(!candidatos.length){ toast('No hay unidades disponibles para despacho', true); return; }
  const u = candidatos[0];
  state.despachoScan.din = u.din;
  document.getElementById('scan-din').textContent = "=" + u.din;
  toast(`Hemocomponente escaneado: ${u.din}`);
  evaluarVerificacion();
}
function escanearPulsera(){
  const req = state.solicitudes[0];
  const hc = req ? req.hc : `HC-2026-${Math.floor(Math.random()*90000+10000)}`;
  state.despachoScan.hc = hc;
  state.despachoScan.paciente = req ? req.paciente : 'Paciente sin solicitud vinculada';
  document.getElementById('scan-hc').textContent = hc;
  toast(`Pulsera escaneada: ${hc}`);
  evaluarVerificacion();
}
function evaluarVerificacion(){
  const { din, hc } = state.despachoScan;
  const box = document.getElementById('verif-rows');
  const btn = document.getElementById('despacho-btn');
  if(!din || !hc){
    box.innerHTML = `<div class="cell-muted" style="font-size:12.5px;">Escanea ambos códigos para ejecutar la doble verificación electrónica.</div>`;
    btn.disabled = true; btn.style.opacity = .5;
    return;
  }
  const u = state.inventory.find(x=>x.din===din);
  const bloqueada = u.status === 'bloqueado' || u.status === 'vencido';
  box.innerHTML = `
    <div class="match-row ${bloqueada?'mismatch':''}"><span>Estado de la unidad</span><strong>${statusMeta[u.status][1]}</strong></div>
    <div class="match-row"><span>Grupo ABO/Rh</span><strong>${u.abo}</strong></div>
    <div class="match-row"><span>N° de historia clínica</span><strong>${hc}</strong></div>
    <div class="match-row"><span>Vigencia de la unidad</span><strong>${u.exp}</strong></div>
  `;
  if(bloqueada){
    btn.disabled = true; btn.style.opacity = .5;
    toast(`La unidad ${din} está ${statusMeta[u.status][1].toLowerCase()} y no puede despacharse`, true);
    return;
  }
  const firmaOk = document.getElementById('despacho-firma')?.checked;
  btn.disabled = !firmaOk; btn.style.opacity = firmaOk ? 1 : .5;
}
function autorizarDespacho(){
  const { din, hc, paciente } = state.despachoScan;
  const u = state.inventory.find(x=>x.din===din);
  if(!u) return;
  u.status = 'despachado';
  pendientes.despachosNuevos.push({ din, hc });
  logAudit(`Autorizó despacho tras doble verificación electrónica y firma electrónica`, `DIN ${din} → ${hc}`);
  toast(`Entrega registrada: ${din} → ${paciente || hc}`);
  state.despachoScan = { din:null, hc:null };
  document.getElementById('scan-din').textContent = '—';
  document.getElementById('scan-hc').textContent = '—';
  document.getElementById('despacho-firma').checked = false;
  renderInventory();
  evaluarVerificacion();
}

/* ---------------- Hemovigilancia (RF-15) ---------------- */
function renderHemovigilancia(){
  document.getElementById('hemovigilancia-rows').innerHTML = state.hemovigilancia.map(e=>{
    const [sColor,sLabel] = e.severidad.split(':');
    const [eColor,eLabel] = e.estado.split(':');
    return `<tr><td class="mono">${e.fecha}</td><td class="cell-strong">${e.paciente}</td><td class="cell-muted">${e.reaccion}</td>
      <td class="mono">${e.din}</td><td>${pill(sColor,sLabel)}</td><td>${pill(eColor,eLabel)}</td></tr>`;
  }).join('');
}
function abrirModalHemovigilancia(){
  const dinOptions = state.inventory.filter(u=>u.status==='despachado').map(u=>`<option value="${u.din}">${u.din}</option>`).join('') || '<option value="">Sin unidades despachadas</option>';
  openModal(`
    <div class="modal-head">
      <div><div class="modal-title">Registrar evento de hemovigilancia</div><div class="card-desc">Trazabilidad hacia la donación origen</div></div>
      <button class="modal-close" onclick="closeModal()">✕</button>
    </div>
    <div style="display:flex; flex-direction:column; gap:12px;">
      <div class="field"><label>Paciente</label><input class="input" id="hv-paciente" data-val="nombre" data-req="1" maxlength="60" autocomplete="off" placeholder="Nombres y apellidos"></div>
      <div class="field"><label>DIN origen</label><select class="input mono" id="hv-din">${dinOptions}</select></div>
      <div class="field"><label>Reacción observada</label><input class="input" id="hv-reaccion" data-val="texto" data-req="1" data-min="4" data-max="100" maxlength="100" autocomplete="off" placeholder="Ej. Reacción febril no hemolítica"></div>
      <div class="field"><label>Severidad</label>
        <select class="input" id="hv-severidad"><option value="amber:Leve">Leve</option><option value="red:Grave">Grave</option></select>
      </div>
    </div>
    <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:18px;">
      <button class="btn btn-ghost btn-sm" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary btn-sm" onclick="guardarEventoHemovigilancia()">Registrar evento</button>
    </div>
  `);
}
function guardarEventoHemovigilancia(){
  const dinSel = document.getElementById('hv-din').value;
  if(!dinSel){ toast('No hay unidades despachadas: no se puede asociar el evento a una donación', true); return; }
  if(!VAL.formulario([
    ['hv-paciente','nombre',{label:'Paciente'}],
    ['hv-reaccion','texto',{label:'Reacción observada'}]
  ])) return;
  const paciente = document.getElementById('hv-paciente').value.trim();
  const evento = {
    id: nuevoId(),
    fecha: nowStr().split(' ')[0], paciente,
    din: dinSel,
    reaccion: document.getElementById('hv-reaccion').value.trim(),
    severidad: document.getElementById('hv-severidad').value,
    estado: 'amber:En investigación',
  };
  state.hemovigilancia.unshift(evento);
  logAudit('Registró evento adverso transfusional', `DIN ${evento.din} · ${paciente}`);
  toast('Evento de hemovigilancia registrado');
  closeModal();
  renderHemovigilancia();
}

/* ---------------- Reportes (RF-16) ---------------- */
const reportsData = [
  ["Reporte operacional mensual","Movimientos de inventario, captación y despacho","PRONAHEBAS"],
  ["Reporte epidemiológico","Prevalencia de marcadores serológicos reactivos","DIGDOT"],
  ["Unidades perdidas y vencidas","Unidades que vencieron o se descartaron","MINSA"],
  ["Auditoría de hemovigilancia","Eventos adversos transfusionales del periodo","PRONAHEBAS"],
  ["Reporte normativo automático","Generación periódica programada para MINSA","PRONAHEBAS"],
  ["Datos para investigación","Exportación sin datos que identifiquen a las personas","MINSA"],
];
function renderReports(){
  document.getElementById('reports-grid').innerHTML = reportsData.map(([title,desc,org],i)=>{
    return `<div class="kpi" style="display:flex; flex-direction:column; gap:8px;">
      <span class="badge-rf" style="align-self:flex-start;">${org}</span>
      <div style="font-weight:600; font-size:13.5px;">${title}</div>
      <div class="hint" style="line-height:1.4;">${desc}</div>
      <div style="display:flex; gap:6px; margin-top:4px;">
        <button class="btn btn-ghost btn-sm" onclick="generarReporte('${title.replace(/'/g,"")}','PDF')">PDF</button>
        <button class="btn btn-ghost btn-sm" onclick="generarReporte('${title.replace(/'/g,"")}','Excel')">Excel</button>
      </div>
    </div>`;
  }).join('');
}
/* ---------------- Contenido real de cada reporte ----------------
   Cada reporte se arma en el momento a partir de los datos que tiene el
   sistema (inventario, donantes, solicitudes, hemovigilancia y auditoría),
   no con cifras escritas a mano: si cambian los datos, cambia el reporte. */
function contarPor(lista, clave){
  return lista.reduce((acc, item) => {
    const k = typeof clave === 'function' ? clave(item) : item[clave];
    acc[k] = (acc[k] || 0) + 1;
    return acc;
  }, {});
}

function filasDesdeConteo(conteo, total){
  return Object.entries(conteo)
    .sort((a, b) => b[1] - a[1])
    .map(([etiqueta, n]) => [etiqueta, n, total ? ((n / total) * 100).toFixed(1) + '%' : '—']);
}

const generadoresDeReporte = {
  'Reporte operacional mensual': () => {
    const inv = state.inventory;
    const estados = contarPor(inv, 'status');
    const disponibles = estados.disponible || 0;
    return {
      columnas: ['Indicador', 'Unidades', 'Participación'],
      filas: [
        ...filasDesdeConteo(estados, inv.length).map(([e, n, p]) => [`Unidades en estado "${statusMeta[e] ? statusMeta[e][1] : e}"`, n, p]),
        ['Donantes registrados', state.donantes.length, '—'],
        ['Solicitudes atendidas o en curso', state.solicitudes.length, '—'],
      ],
      resumen: `${inv.length} unidades en total · ${disponibles} disponibles para despacho`,
    };
  },
  'Reporte epidemiológico': () => {
    const enCuarentena = state.inventory.filter(u => u.status === 'cuarentena').length;
    const bloqueadas = state.inventory.filter(u => u.status === 'bloqueado').length;
    const porGrupo = contarPor(state.donantes, 'abo');
    return {
      columnas: ['Grupo sanguíneo', 'Donantes', 'Participación'],
      filas: filasDesdeConteo(porGrupo, state.donantes.length),
      resumen: `${enCuarentena} unidades en cuarentena a la espera de resultados · ${bloqueadas} bloqueadas por reactividad`,
    };
  },
  'Unidades perdidas y vencidas': () => {
    const perdidas = state.inventory.filter(u => u.status === 'vencido' || u.status === 'incinerado');
    return {
      columnas: ['Código de la donación', 'Hemocomponente', 'Vence', 'Situación'],
      filas: perdidas.map(u => [u.din, u.producto, u.exp, statusMeta[u.status][1]]),
      resumen: perdidas.length
        ? `${perdidas.length} unidades perdidas de ${state.inventory.length} (${((perdidas.length / state.inventory.length) * 100).toFixed(1)}% de merma)`
        : 'Sin unidades perdidas en el periodo',
    };
  },
  'Auditoría de hemovigilancia': () => ({
    columnas: ['Fecha', 'Paciente', 'Reacción', 'Severidad', 'Estado'],
    filas: state.hemovigilancia.map(e => [e.fecha, e.paciente, e.reaccion,
      e.severidad.split(':')[1], e.estado.split(':')[1]]),
    resumen: `${state.hemovigilancia.length} eventos registrados · ` +
      `${state.hemovigilancia.filter(e => e.severidad.startsWith('red')).length} de severidad grave`,
  }),
  'Reporte normativo automático': () => {
    const acciones = contarPor(state.auditLog, fila => fila[1]);
    return {
      columnas: ['Usuario responsable', 'Acciones registradas', 'Participación'],
      filas: filasDesdeConteo(acciones, state.auditLog.length),
      resumen: `${state.auditLog.length} acciones registradas en la bitácora del sistema`,
    };
  },
  'Datos para investigación': () => {
    const porProducto = contarPor(state.inventory, 'producto');
    return {
      columnas: ['Hemocomponente', 'Unidades', 'Participación'],
      filas: filasDesdeConteo(porProducto, state.inventory.length),
      resumen: 'Conjunto sin nombres, documentos ni historias clínicas',
    };
  },
};

function generarReporte(title, formato){
  const generador = generadoresDeReporte[title];
  if(!generador){ toast('Ese reporte todavía no está disponible', true); return; }
  const reporte = generador();
  const fecha = nowStr();
  const entidad = (reportsData.find(r => r[0] === title) || [,, 'MINSA'])[2];
  const codigo = 'RPT-' + String(Math.floor(Math.random() * 9000) + 1000) + '-2026';
  window.reporteActual = { title, fecha, entidad, codigo, ...reporte };
  logAudit(`Generó el reporte "${title}" en formato ${formato}`, `${reporte.filas.length} filas`);

  if(formato === 'Excel'){ descargarReporte(); return; }

  // Vista del documento, con la forma de un reporte oficial impreso
  const cuerpo = reporte.filas.length
    ? reporte.filas.map(fila => `<tr>${fila.map((c, i) =>
        i === 0 ? `<td class="cell-strong">${c}</td>` : `<td class="mono">${c}</td>`).join('')}</tr>`).join('')
    : `<tr><td colspan="${reporte.columnas.length}" class="cell-muted">Sin datos en el periodo</td></tr>`;

  openModal(`
    <div class="modal-head no-print">
      <div><div class="modal-title">Vista previa del reporte</div>
      <div class="card-desc">Así se imprime o se guarda en PDF</div></div>
      <button class="modal-close" onclick="closeModal()">✕</button>
    </div>
    <div class="doc-reporte">
      <div class="doc-membrete">
        <div>
          <div class="doc-institucion">Hospital de Lima Este Vitarte</div>
          <div class="doc-area">Servicio de Hemoterapia y Banco de Sangre — Tipo II</div>
        </div>
        <div class="doc-codigo">${codigo}</div>
      </div>
      <h2 class="doc-titulo">${title}</h2>
      <div class="doc-datos">
        <div><span>Dirigido a</span><strong>${entidad}</strong></div>
        <div><span>Fecha de emisión</span><strong>${fecha}</strong></div>
        <div><span>Responsable</span><strong>${state.currentUser}</strong></div>
        <div><span>Cargo</span><strong>${state.currentRole}</strong></div>
      </div>
      <p class="doc-resumen"><strong>Resumen:</strong> ${reporte.resumen}</p>
      <table>
        <thead><tr>${reporte.columnas.map(c => `<th>${c}</th>`).join('')}</tr></thead>
        <tbody>${cuerpo}</tbody>
      </table>
      <div class="doc-firma">
        <div class="doc-linea"></div>
        <div>${state.currentUser}</div>
        <div class="doc-cargo">${state.currentRole} · Hospital de Lima Este Vitarte</div>
      </div>
    </div>
    <div class="no-print" style="display:flex; gap:8px; flex-wrap:wrap; margin-top:14px;">
      <button class="btn btn-primary btn-sm" onclick="imprimirReporte()">Imprimir o guardar en PDF</button>
      <button class="btn btn-ghost btn-sm" onclick="descargarReporte()">Descargar en Excel (CSV)</button>
      <button class="btn btn-ghost btn-sm" onclick="closeModal()">Cerrar</button>
    </div>
  `);
}

function imprimirReporte(){
  logAudit(`Imprimió el reporte "${window.reporteActual.title}"`, window.reporteActual.codigo);
  window.print();
}

function descargarReporte(){
  const r = window.reporteActual;
  if(!r) return;
  const lineas = [
    ['Hospital de Lima Este Vitarte — Servicio de Hemoterapia y Banco de Sangre'],
    [r.title], [`Documento ${r.codigo} · dirigido a ${r.entidad}`],
    [`Emitido el ${r.fecha} por ${state.currentUser} (${state.currentRole})`],
    [r.resumen], [], r.columnas, ...r.filas];
  window.csvInventario = lineas
    .map(fila => fila.map(celda => `"${String(celda).replace(/"/g, '""')}"`).join(';'))
    .join('\r\n');
  const nombre = r.title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '') + '.csv';
  const blob = new Blob(['﻿' + window.csvInventario], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url; enlace.download = nombre;
  document.body.appendChild(enlace); enlace.click(); enlace.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  logAudit(`Descargó el reporte "${r.title}"`, nombre);
  toast(`Reporte descargado: ${nombre}`);
  closeModal();
}

/* ---------------- Auditoría (RF-17) ---------------- */
function renderAudit(){
  document.getElementById('audit-rows').innerHTML = state.auditLog.slice(0,50).map(([ts,user,action,resource,ip])=>{
    return `<tr><td class="mono">${ts}</td><td class="cell-strong">${user}</td><td class="cell-muted">${action}</td><td class="mono">${resource}</td><td class="mono">${ip}</td></tr>`;
  }).join('');
}

/* ---------------- RF-23 Monitoreo de temperatura ---------------- */
state.temps = [];
function renderTemps(){
  const el = document.getElementById('temp-grid');
  if(!el) return;
  el.innerHTML = state.temps.map(t=>`
    <div style="padding:12px 14px; border-radius:10px; background:${t.ok?'var(--bg)':'var(--red-50)'};">
      <div class="cell-muted" style="margin-bottom:4px;">${t.camara}</div>
      <div style="font-weight:700; font-size:20px; color:${t.ok?'var(--ink-900)':'var(--red-700)'};">${t.actual}°C</div>
      <div class="cell-muted" style="margin-top:4px;">Rango: ${t.rango}</div>
      ${t.ok ? '' : `<div style="margin-top:6px;">${pill('red','Fuera de rango')}</div>`}
    </div>`).join('');
}

/* ---------------- RF-18/19/40/45 Red interhospitalaria ---------------- */
state.establecimientos = [];
function renderConvenios(){
  const body = document.getElementById('convenios-rows');
  if(!body) return;
  const hoy = new Date(); hoy.setHours(0,0,0,0);
  body.innerHTML = state.establecimientos.map(e => {
    let estado = pill('blue','Indefinido');
    if(e.hasta && e.hasta !== '—'){
      const [d,m,a] = e.hasta.split('/').map(Number);
      estado = new Date(a, m-1, d) < hoy ? pill('red','Vencido') : pill('green','Vigente');
    }
    return `<tr><td class="cell-strong">${e.nombre}</td><td class="cell-muted">${e.convenio}</td><td class="mono">${e.hasta || '—'}</td><td>${estado}</td></tr>`;
  }).join('');
}

/** Solo Administrador: borra todo y vuelve a cargar los datos de ejemplo de la base */
function restablecerDatos(){
  if(!MODO_SERVIDOR){ reiniciarDatosLocales(); location.reload(); return; }
  if(!confirm('Se borrarán todos los datos guardados y se cargarán de nuevo los datos de ejemplo. ¿Continuar?')) return;
  fetch('/api/restablecer', { method: 'POST', credentials: 'same-origin' })
    .then(r => r.json())
    .then(j => { if(!j.ok) throw new Error(j.error); location.reload(); })
    .catch(e => toast('No se pudo restablecer: ' + e.message, true));
}

state.intercambios = [];
function renderIntercambios(){
  const body = document.getElementById('intercambio-rows');
  if(!body) return;
  body.innerHTML = state.intercambios.map((x,i)=>{
    const [c,l] = x.estado.split(':');
    return `<tr><td class="cell-strong">${x.establecimiento}</td><td class="cell-muted">${x.producto}</td><td class="mono">${x.cantidad}</td>
      <td>${pill(x.direccion==='Saliente'?'blue':'purple', x.direccion)}</td><td>${pill(c,l)}</td>
      <td>${x.estado.startsWith('amber')?`<button class="btn btn-ghost btn-sm" onclick="responderIntercambio(${i})">Responder</button>`:x.estado.startsWith('green')?`<button class="btn btn-ghost btn-sm" onclick="devolverIntercambio(${i})">Registrar devolución</button>`:''}</td></tr>`;
  }).join('');
}
function abrirModalIntercambio(){
  openModal(`
    <div class="modal-head"><div><div class="modal-title">Nueva solicitud de intercambio</div>
    <div class="card-desc">Pedido de unidades a otro establecimiento de la red</div></div>
    <button class="modal-close" onclick="closeModal()">✕</button></div>
    <div style="display:flex; flex-direction:column; gap:12px;">
      <div class="field"><label>Establecimiento</label>
        <select class="input" id="ix-est"><option>Hospital Vitarte II</option><option>Hospital de la Solidaridad ATE</option><option>PRONAHEBAS Central</option></select>
      </div>
      <div class="grid-2">
        <div class="field"><label>Hemocomponente</label>
          <select class="input" id="ix-comp">
            <option>Concentrado de Hematíes</option>
            <option>Plasma Fresco Congelado</option>
            <option>Concentrado de Plaquetas</option>
            <option>Crioprecipitado</option>
          </select>
        </div>
        <div class="field"><label>Grupo sanguíneo</label>
          <select class="input" id="ix-abo">
            <option>O NEG</option><option>O POS</option><option>A NEG</option><option>A POS</option>
            <option>B NEG</option><option>B POS</option><option>AB NEG</option><option>AB POS</option>
          </select>
        </div>
        <div class="field"><label>Cantidad (unidades)</label>
          <input class="input" id="ix-cant" data-val="entero" data-req="1" data-min="1" data-max="50" inputmode="numeric" maxlength="2" autocomplete="off" value="2">
        </div>
        <div class="field"><label>Dirección</label>
          <select class="input" id="ix-dir"><option>Saliente</option><option>Entrante</option></select>
        </div>
      </div>
      <div class="field"><label>Motivo del pedido</label>
        <input class="input" id="ix-motivo" data-val="texto" data-req="1" data-min="5" data-max="100" maxlength="100" autocomplete="off" placeholder="Ej. stock crítico de O− en emergencia">
      </div>
    </div>
    <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:18px;">
      <button class="btn btn-ghost btn-sm" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary btn-sm" onclick="guardarIntercambio()">Enviar solicitud</button>
    </div>`);
}
function guardarIntercambio(){
  const est = document.getElementById('ix-est').value;
  const comp = document.getElementById('ix-comp').value;
  const abo = document.getElementById('ix-abo').value;
  if(!VAL.formulario([
    ['ix-cant','entero',{label:'Cantidad'}],
    ['ix-motivo','texto',{label:'Motivo del pedido'}]
  ])) return;
  const cantidad = parseInt(document.getElementById('ix-cant').value, 10);
  const direccion = document.getElementById('ix-dir').value;
  const motivo = document.getElementById('ix-motivo').value.trim();
  state.intercambios.unshift({
    id: nuevoId(),
    establecimiento: est,
    producto: `${comp} ${abo}`,
    componente: comp, grupo: abo, unidades: cantidad, motivo,
    cantidad: `${cantidad} un.`,
    direccion,
    estado: 'amber:Pendiente',
  });
  logAudit(`Envió solicitud de intercambio${motivo ? ' — ' + motivo : ''}`,
           `${est} · ${cantidad} un. de ${comp} ${abo}`);
  toast(`Solicitud enviada a ${est}: ${cantidad} unidades de ${comp} ${abo}`);
  closeModal();
  renderIntercambios();
}
function responderIntercambio(i){
  state.intercambios[i].estado = 'green:Aceptada';
  logAudit('Registró respuesta a solicitud de intercambio', state.intercambios[i].establecimiento);
  toast('Intercambio aceptado');
  renderIntercambios();
}
function devolverIntercambio(i){
  logAudit('Registró devolución de unidades prestadas', state.intercambios[i].establecimiento);
  toast('Devolución registrada — inventario de origen actualizado');
  state.intercambios[i].estado = 'blue:Devuelta';
  renderIntercambios();
}

/* ---------------- RF-20/21 App móvil ---------------- */
function simularPushMovil(){
  pushAlert('blue', 'Notificación push enviada', 'Personal asistencial notificado desde app móvil');
  logAudit('Envió notificación push a la aplicación móvil', 'App móvil');
  toast('Notificación push simulada enviada al personal');
}


/* ---------------- Administración: Usuarios (RF-26) ---------------- */
state.usuarios = [];
function renderUsuarios(){
  const body = document.getElementById('usuarios-rows');
  if(!body) return;
  body.innerHTML = state.usuarios.map((u,i)=>`
    <tr><td class="cell-strong">${u.nombre}</td><td class="cell-muted">${u.correo || '—'}</td><td class="cell-muted">${u.rol}</td><td class="mono">${u.ultimo}</td>
    <td>${u.activo?pill('green','Activo'):pill('slate','Desactivado')}</td>
    <td><button class="btn btn-ghost btn-sm" onclick="toggleUsuario(${i})">${u.activo?'Desactivar':'Reactivar'}</button></td></tr>`).join('');
}
function abrirModalUsuario(){
  openModal(`
    <div class="modal-head"><div><div class="modal-title">Nuevo usuario</div>
    <div class="card-desc">La cuenta verá solo los módulos que su rol permite</div></div>
    <button class="modal-close" onclick="closeModal()">✕</button></div>
    <div style="display:flex; flex-direction:column; gap:12px;">
      <div class="field"><label>Nombres y apellidos</label>
        <input class="input" id="us-nombre" data-val="nombre" data-req="1" maxlength="60" autocomplete="off" placeholder="Ej. José William Palomino Camarena">
      </div>
      <div class="grid-2">
        <div class="field"><label>DNI</label>
          <input class="input" id="us-dni" data-val="dni" data-req="1" inputmode="numeric" maxlength="8" autocomplete="off" placeholder="8 dígitos">
        </div>
        <div class="field"><label>Correo institucional</label>
          <input class="input" id="us-correo" data-val="correo" data-req="1" maxlength="60" autocomplete="off" placeholder="usuario@hlev.gob.pe">
        </div>
        <div class="field"><label>Rol</label>
          <select class="input" id="us-rol">
            <option>Médico Solicitante</option>
            <option>Tecnólogo Médico</option>
            <option>Jefe de Banco de Sangre</option>
            <option>Administrador</option>
          </select>
        </div>
      </div>
      <div class="checkline">
        <input type="checkbox" id="us-activo" checked>
        <label for="us-activo" style="font-weight:400;">La cuenta queda activa desde ahora</label>
      </div>
    </div>
    <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:18px;">
      <button class="btn btn-ghost btn-sm" onclick="closeModal()">Cancelar</button>
      <button class="btn btn-primary btn-sm" onclick="guardarUsuario()">Crear cuenta</button>
    </div>`);
}

function guardarUsuario(){
  if(!VAL.formulario([
    ['us-nombre','nombre',{label:'Nombres y apellidos'}],
    ['us-dni','dni',{label:'DNI'}],
    ['us-correo','correo',{label:'Correo institucional'}]
  ])) return;
  const nombre = document.getElementById('us-nombre').value.trim();
  const dni = document.getElementById('us-dni').value.trim();
  const correo = document.getElementById('us-correo').value.trim().toLowerCase();
  if(state.usuarios.some(u => u.correo === correo || u.dni === dni)){ toast('Ya existe una cuenta con ese DNI o correo', true); return; }
  const rol = document.getElementById('us-rol').value;
  const activo = document.getElementById('us-activo').checked;
  state.usuarios.unshift({ nombre, rol, dni, correo, ultimo: 'Sin ingresar aún', activo });
  logAudit(`Creó la cuenta de un usuario con rol ${rol}`, nombre);
  toast(`${nombre} quedó registrado como ${rol}`);
  closeModal();
  renderUsuarios();
}

function toggleUsuario(i){
  state.usuarios[i].activo = !state.usuarios[i].activo;
  logAudit(`${state.usuarios[i].activo?'Reactivó':'Desactivó'} cuenta de usuario`, state.usuarios[i].nombre);
  toast(`${state.usuarios[i].nombre} ${state.usuarios[i].activo?'reactivado':'desactivado'}`);
  renderUsuarios();
}

document.querySelectorAll('.tab').forEach(tab=>{
  tab.addEventListener('click', ()=>{
    const [group, name] = tab.dataset.tab.split(':');
    document.querySelectorAll(`.tab`).forEach(t=>{
      if(t.dataset.tab.startsWith(group+':')) t.classList.toggle('active', t===tab);
    });
    document.querySelectorAll(`[data-tabpanel^="${group}:"]`).forEach(p=>{
      p.style.display = (p.dataset.tabpanel===tab.dataset.tab) ? '' : 'none';
    });
    if(tab.dataset.tab === 'sol:compat') fillCompatSelect();
  });
});