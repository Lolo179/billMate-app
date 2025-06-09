console.log("✅ JS facturas-cliente cargado");

let facturasCliente = [];
let paginaActual = 1;
const porPagina = 10;

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");
    const clientId = document.body.getAttribute("data-client-id");

    if (!token || !clientId) {
        console.warn("❌ Token o clienteId faltante");
        window.location.href = "/login";
        return;
    }

    fetch(`http://localhost:8080/billing/invoices/client/${clientId}`, {
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("Error al obtener facturas");
        return res.json();
    })
    .then(data => {
        facturasCliente = data;
        paginaActual = 1;
        renderPaginaFacturas();
    })
    .catch(err => {
        console.error("❌", err);
        window.location.href = "/clientes";
    });
});

function renderPaginaFacturas() {
    const tbody = document.getElementById("facturas-por-cliente");
    tbody.innerHTML = "";

    const inicio = (paginaActual - 1) * porPagina;
    const fin = inicio + porPagina;
    const pagina = facturasCliente.slice(inicio, fin);

    if (pagina.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5">⚠️ No hay facturas para mostrar</td></tr>`;
        return;
    }

    pagina.forEach(f => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${f.invoiceId}</td>
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

    renderPaginacionFacturas();
}

function renderPaginacionFacturas() {
    const totalPaginas = Math.ceil(facturasCliente.length / porPagina);
    const ul = document.getElementById("paginacion-facturas-cliente");
    ul.innerHTML = "";

    const crearLi = (label, pagina, disabled = false, active = false) => {
        const li = document.createElement("li");
        li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
        li.addEventListener("click", e => {
            e.preventDefault();
            if (!disabled) {
                paginaActual = pagina;
                renderPaginaFacturas();
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

function descargarPDF(invoiceId) {
    const token = localStorage.getItem("jwt");
    fetch(`/billing/invoices/${invoiceId}/pdf`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.blob();
    })
    .then(blob => {
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank");
    })
    .catch(err => {
        console.error("❌ Error al abrir PDF:", err);
        alert("No se pudo abrir el PDF.");
    });
}

function editarFactura(invoiceId) {
    window.location.href = `/facturas/${invoiceId}/editar`;
}

function getEstadoClase(status) {
    switch (status) {
        case "PAID": return "badge-success";
        case "SENT": return "badge-info";
        case "DRAFT": return "badge-secondary";
        default: return "badge-light";
    }
}
