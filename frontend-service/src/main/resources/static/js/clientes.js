document.addEventListener("DOMContentLoaded", async function () {
    const token = localStorage.getItem("jwt");
    if (!token) {
        return window.location.href = "/login";
    }

    try {
        const response = await fetch("http://localhost:8080/billing/clients", {
            headers: { "Authorization": "Bearer " + token }
        });

        if (!response.ok) throw new Error("Token inválido");

        const clientes = await response.json();
        const tbody = document.getElementById("clientes-body");
        tbody.innerHTML = "";

        clientesData = clientes;
        paginaClientes = 1;
        renderClientesPagina();


        // Filtro
        document.getElementById("cliente-filter").addEventListener("input", function () {
            const filtro = this.value.toLowerCase();
            document.querySelectorAll("#clientes-body tr").forEach(row => {
                const nombre = row.children[0].innerText.toLowerCase();
                row.style.display = nombre.includes(filtro) ? "" : "none";
            });
        });

    } catch (err) {
        console.error(err);
        window.location.href = "/login";
    }
});

let clientesData = [];
let paginaClientes = 1;
const clientesPorPagina = 10;


function verDetalleCliente(cliente) {
    document.getElementById("clienteNombre").innerText = cliente.name;
    document.getElementById("clienteEmail").innerText = cliente.email;
    document.getElementById("clienteTelefono").innerText = cliente.phone;
    document.getElementById("clienteNif").innerText = cliente.nif;
    document.getElementById("clienteDireccion").innerText = cliente.address;

    document.getElementById("btnNuevaFactura").onclick = function () {
        window.location.href = `/facturas/nueva/${cliente.clientId}`;
    };

    document.getElementById("btnVerFacturas").onclick = function () {
            window.location.href = `/clientes/${cliente.clientId}/facturas`;
        };

    $('#clienteModal').modal('show');
}

function verFacturasCliente(clienteId) {
    window.location.href = `/clientes/${clienteId}/facturas`;
}

function renderClientesPagina() {
    const tbody = document.getElementById("clientes-body");
    tbody.innerHTML = "";

    const inicio = (paginaClientes - 1) * clientesPorPagina;
    const fin = inicio + clientesPorPagina;
    const pagina = clientesData.slice(inicio, fin);

    if (pagina.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6">⚠️ No hay clientes disponibles</td></tr>`;
        return;
    }

    pagina.forEach(cliente => {
        const tr = document.createElement("tr");
        tr.classList.add("cliente-row");
        tr.style.cursor = "pointer";

        tr.innerHTML = `
            <td>${cliente.name}</td>
            <td>${cliente.email}</td>
            <td>${cliente.phone}</td>
            <td>${cliente.nif}</td>
            <td>${cliente.address}</td>
            <td>${new Date(cliente.createdAt).toLocaleDateString()}</td>
        `;

        tr.addEventListener("click", () => verDetalleCliente(cliente));
        tbody.appendChild(tr);
    });

    renderClientesPaginacion();
}
function renderClientesPaginacion() {
    const totalPaginas = Math.ceil(clientesData.length / clientesPorPagina);
    const ul = document.getElementById("paginacion-clientes");
    ul.innerHTML = "";

    const crearLi = (label, pagina, disabled = false, active = false) => {
        const li = document.createElement("li");
        li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
        li.addEventListener("click", e => {
            e.preventDefault();
            if (!disabled) {
                paginaClientes = pagina;
                renderClientesPagina();
            }
        });
        return li;
    };

    ul.appendChild(crearLi("«", paginaClientes - 1, paginaClientes === 1));

    for (let i = 1; i <= totalPaginas; i++) {
        ul.appendChild(crearLi(i, i, false, paginaClientes === i));
    }

    ul.appendChild(crearLi("»", paginaClientes + 1, paginaClientes === totalPaginas));
}
