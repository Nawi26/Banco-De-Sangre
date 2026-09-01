// ============================================================================
// LÓGICA FRONTEND - BANCO DE SANGRE HLEV
// ============================================================================

// 1. Captura de Elementos del DOM (Autenticación)
const loginView = document.getElementById("loginView");
const dashboardView = document.getElementById("dashboardView");
const loginForm = document.getElementById("loginForm");
const loginError = document.getElementById("loginError");
const logoutBtn = document.getElementById("logoutBtn");

// Captura de Elementos del DOM (Panel de Control)
const roleBadge = document.getElementById("roleBadge");
const userNameDisplay = document.getElementById("userNameDisplay");
const navUserManagement = document.getElementById("navUserManagement");
const navInventory = document.getElementById("navInventory");
const inventorySection = document.getElementById("inventorySection");
const userManagementSection = document.getElementById("userManagementSection");

// ============================================================================
// MÓDULO DE AUTENTICACIÓN
// ============================================================================
loginForm.addEventListener("submit", async function(event) {
    event.preventDefault();
    
    const identificador = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    const btnSubmit = loginForm.querySelector("button[type='submit']");

    // Limpiar errores previos y mostrar estado de carga
    loginError.textContent = "";
    btnSubmit.textContent = "Validando credenciales...";
    btnSubmit.disabled = true;

    try {
        const response = await fetch('http://localhost:3000/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identificador, password })
        });

        const data = await response.json();

        // Validar si el backend devolvió un error (Cuenta no existe, mal password, etc.)
        if (!response.ok) {
            loginError.textContent = data.mensaje;
            btnSubmit.textContent = "Ingresar al Sistema";
            btnSubmit.disabled = false;
            return;
        }

        // Acceso Exitoso
        configurarPanel(data.usuario.rol, data.usuario.nombre_completo);
        
        loginView.style.display = "none";
        dashboardView.style.display = "flex";
        
        // Cargar datos de la base de datos automáticamente
        fetchInventory();

    } catch (error) {
        console.error("Fallo de red:", error);
        loginError.textContent = "Error: No se pudo contactar con el servidor central (Verifique que Node.js esté encendido).";
    } finally {
        btnSubmit.textContent = "Ingresar al Sistema";
        btnSubmit.disabled = false;
    }
});

// Cerrar Sesión
logoutBtn.addEventListener("click", function() {
    dashboardView.style.display = "none";
    loginView.style.display = "flex";
    loginForm.reset();
});

// ============================================================================
// MÓDULO DE CONTROL DE ACCESO BASADO EN ROLES (RBAC)
// ============================================================================
function configurarPanel(rol, nombreCompleto) {
    roleBadge.textContent = rol;
    userNameDisplay.textContent = nombreCompleto;
    
    // Si es Administrador, habilita la gestión de cuentas
    if (rol.toUpperCase() === 'ADMINISTRADOR') {
        if(navUserManagement) navUserManagement.style.display = "block";
    } else {
        if(navUserManagement) navUserManagement.style.display = "none";
    }
    
    mostrarSeccion(inventorySection);
}

// ============================================================================
// NAVEGACIÓN DEL PANEL
// ============================================================================
if(navUserManagement) {
    navUserManagement.addEventListener("click", (e) => {
        e.preventDefault();
        mostrarSeccion(userManagementSection);
    });
}

if(navInventory) {
    navInventory.addEventListener("click", (e) => {
        e.preventDefault();
        mostrarSeccion(inventorySection);
    });
}

function mostrarSeccion(seccionActiva) {
    if(inventorySection) inventorySection.style.display = "none";
    if(userManagementSection) userManagementSection.style.display = "none";
    if(seccionActiva) seccionActiva.style.display = "block";
}

// ============================================================================
// MÓDULO DE INVENTARIO (Consumo RESTful)
// ============================================================================
async function fetchInventory() {
    try {
        const response = await fetch('http://localhost:3000/api/inventario');
        if (!response.ok) throw new Error('Fallo al obtener inventario');
        
        const realInventory = await response.json();
        renderInventory(realInventory);
    } catch (error) {
        console.error('Error de sistema:', error);
    }
}

function renderInventory(inventoryData) {
    const tableBody = document.getElementById("inventoryBody"); 
    if(!tableBody) return;
    
    tableBody.innerHTML = "";
    
    inventoryData.forEach(item => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><strong>${item.codigo_producto_isbt}</strong></td>
            <td>${item.tipo_hemocomponente}</td>
            <td>${item.grupo_abo} ${item.factor_rh}</td>
            <td>${new Date(item.fecha_vencimiento).toLocaleDateString()}</td>
            <td style="color: green; font-weight: 500;">${item.estado}</td>
        `;
        tableBody.appendChild(row);
    });
}
