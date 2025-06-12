console.log("🧾 facturas.js cargado");

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");

    if (!token) {
        console.warn("❌ No hay token. Redirigiendo al login...");
        window.location.href = "/login";
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const clienteId = urlParams.get("clienteId");

    if (clienteId) {
        // Mostrar facturas del cliente
        fetch(`/billing/invoices/client/${clienteId}`, {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        })
            .then(res => {
                if (!res.ok) throw new Error("No se pudieron obtener las facturas del cliente");
                return res.json();
            })
            .then(data => mostrarFacturas(data))
            .catch(err => {
                console.error("Error al obtener facturas por cliente:", err);
                window.location.href = "/login";
            });

    } else {
    fetch("/billing/invoices", {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        })
            .then(res => {
                if (!res.ok) throw new Error("Error al obtener todas las facturas");
                return res.json();
            })
            .then(data => mostrarFacturas(data))
            .catch(err => {
                console.error("❌ Error al listar facturas:", err);
                document.getElementById("facturas-table").innerHTML = `<tr><td colspan="6">❌ No se pudieron cargar las facturas</td></tr>`;
            });
        // 🔍 Buscar factura por ID
        const form = document.querySelector("form");
        form.addEventListener("submit", e => {
            e.preventDefault();
            const ref = document.getElementById("busqueda").value.trim();
            if (!ref) return;

            fetch(`/billing/invoices/${ref}`, {
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            })
                .then(res => {
                    if (!res.ok) throw new Error("Factura no encontrada");
                    return res.json();
                })
                .then(data => mostrarFacturas([data]))
                .catch(err => {
                    console.error("Error al buscar factura:", err);
                    document.getElementById("facturas-table").innerHTML = `<tr><td colspan="6">❌ Factura no encontrada</td></tr>`;
                });
        });
    }
});

let facturasData = []; // todas las facturas cargadas
let paginaActual = 1;
const porPagina = 10;

function mostrarFacturas(facturas) {
    facturasData = facturas;
    paginaActual = 1;
    renderPagina();
}


function renderPagina() {
    const tbody = document.getElementById("facturas-table");
    tbody.innerHTML = "";

    const inicio = (paginaActual - 1) * porPagina;
    const fin = inicio + porPagina;
    const pagina = facturasData.slice(inicio, fin);

    if (pagina.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6">⚠️ No hay datos para mostrar</td></tr>`;
        return;
    }

    pagina.forEach(f => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${f.invoiceId}</td>
            <td>${f.clientName}</td>
            <td>${new Date(f.date).toLocaleDateString()}</td>
            <td>${(f.total ?? 0).toFixed(2)} €</td>
            <td><span class="badge ${getEstadoClase(f.status)}">${f.status}</span></td>
            <td>
                <button onclick="descargarPDF(${f.invoiceId})" class="btn btn-sm btn-outline-primary"
                    ${f.status === 'SENT' || f.status === 'PAID' ? '' : 'disabled'}>
                    📎 Ver PDF
                </button>
                <button class="btn btn-sm btn-outline-secondary ml-2" onclick="editarFactura(${f.invoiceId})">✏️ Editar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    renderPaginacion();
}

function renderPaginacion() {
    const totalPaginas = Math.ceil(facturasData.length / porPagina);
    const ul = document.getElementById("paginacion");
    ul.innerHTML = "";

    const crearLi = (label, pagina, disabled = false, active = false) => {
        const li = document.createElement("li");
        li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
        li.addEventListener("click", e => {
            e.preventDefault();
            if (!disabled) {
                paginaActual = pagina;
                renderPagina();
            }
        });
        return li;
    };

    ul.appendChild(crearLi("«", paginaActual - 1, paginaActual === 1));

    for (let i = 1; i <= totalPaginas; i++) {
        ul.appendChild(crearLi(i, i, false, paginaActual === i));
    }

    ul.appendChild(crearLi("»", paginaActual + 1, paginaActual === totalPaginas));
}


function editarFactura(invoiceId) {
    window.location.href = `/facturas/${invoiceId}/editar`;
}

function descargarPDF(invoiceId) {
    const token = localStorage.getItem("jwt");
    if (!token) {
        window.location.href = "/login";
        return;
    }

    fetch(`/billing/invoices/${invoiceId}/pdf`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
    .then(res => {
        if (!res.ok) throw new Error(`Error HTTP ${res.status}`);
        return res.blob();
    })
    .then(blob => {
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank");
    })
    .catch(err => {
        console.error("❌ Error al abrir PDF:", err);
        alert("No se pudo abrir el PDF. Detalles: " + err.message);
    });
}



function getEstadoClase(status) {
    switch (status) {
        case "PAID": return "badge-success";
        case "SENT": return "badge-info";
        case "CANCELLED": return "badge-danger";
        case "DRAFT": return "badge-secondary";
        default: return "badge-light";
    }
}

