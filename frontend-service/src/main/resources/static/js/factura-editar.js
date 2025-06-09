document.addEventListener("DOMContentLoaded", async function () {
    const token = localStorage.getItem("jwt");
    const parts = window.location.pathname.split("/");

    let invoiceId = null;
    if (parts.length >= 3 && parts[1] === "facturas" && !isNaN(parts[2])) {
        invoiceId = parts[2];
    }

    if (!token) {
        window.location.href = "/login";
        return;
    }

    if (!invoiceId) {
        alert("❌ ID de factura no válido en la URL.");
        return;
    }

    try {
        console.log("📡 Llamando a:", `/billing/invoices/${invoiceId}`);

        const res = await fetch(`/billing/invoices/${invoiceId}`, {
            headers: { "Authorization": "Bearer " + token }
        });

        console.log("🔁 Respuesta completa:", res);
        if (!res.ok) {
            const errorBody = await res.text();
            console.error("❌ Status:", res.status);
            console.error("❌ Body:", errorBody);
            throw new Error("Factura no encontrada");
        }

        const factura = await res.json();
        cargarFactura(factura, token);
    } catch (e) {
        console.error("❌ Error al cargar factura:", e);
        alert("❌ No se pudo cargar la factura. Revisa consola para más información.");
    }
});


let currentClientId = null;

function cargarFactura(factura, token) {
    currentClientId = factura.clientId; // ✅ lo almacenamos aquí
    console.log("✅ Factura recibida:", factura);
    console.log("📦 Líneas recibidas:", factura.invoiceLines);


    document.getElementById("descripcion").value = factura.description || "";
    document.getElementById("fecha").value = factura.date || "";
    document.getElementById("estado").value = factura.status || "DRAFT";

console.log("📦 Líneas recibidas (JSON):", JSON.stringify(factura.invoiceLines, null, 2));


    cargarLineas(factura.invoiceLines || []);

    // Obtener info del cliente
    fetch(`/billing/clients/${factura.clientId}`, {
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => res.json())
        .then(cliente => {
            document.getElementById("clienteInfo").value = `${cliente.name} (${cliente.email})`;
        })
        .catch(() => {
            document.getElementById("clienteInfo").value = "❌ Error al cargar cliente";
        });

    actualizarTotal();
    configurarBotones(factura.status, factura.invoiceId);
}



function agregarLinea(linea = {}) {
    const tbody = document.getElementById("lineas-factura");
    const tr = document.createElement("tr");

    const descripcion = linea.description ?? '';
    const cantidad = linea.quantity ?? 1;
    const precio = linea.unitPrice ?? 0;

    tr.innerHTML = `
        <td><input class="form-control descripcion-linea" value="${descripcion}"></td>
        <td><input type="number" class="form-control cantidad-linea" value="${cantidad}" min="1"></td>
        <td><input type="number" class="form-control precio-linea" value="${precio}" step="0.01"></td>
        <td class="total-linea text-right align-middle">0.00 €</td>
        <td><button class="btn btn-sm btn-danger" onclick="eliminarLinea(this)">🗑</button></td>
    `;

    tbody.appendChild(tr);
    recalcularLinea(tr);

    tr.querySelector(".cantidad-linea").addEventListener("input", () => recalcularLinea(tr));
    tr.querySelector(".precio-linea").addEventListener("input", () => recalcularLinea(tr));
}


function eliminarLinea(btn) {
    btn.closest("tr").remove();
    actualizarTotal();
}



function recalcularLinea(tr) {
    const cantidad = parseFloat(tr.querySelector(".cantidad-linea").value) || 0;
    const precio = parseFloat(tr.querySelector(".precio-linea").value) || 0;
    const total = cantidad * precio;
    tr.querySelector(".total-linea").textContent = total.toFixed(2) + " €";
    actualizarTotal();
}


function actualizarTotal() {
    const filas = document.querySelectorAll("#lineas-factura tr");
    let total = 0;
    filas.forEach(tr => {
        const cantidad = parseFloat(tr.querySelector(".cantidad-linea").value) || 0;
        const precio = parseFloat(tr.querySelector(".precio-linea").value) || 0;
        total += cantidad * precio;
    });

    const iva = total * 0.21;
    const totalConIVA = total + iva;

    document.getElementById("totalFactura").textContent = totalConIVA.toFixed(2) + " €";
}

function cargarLineas(lineas) {
    const tbody = document.getElementById("lineas-factura");
    tbody.innerHTML = ""; // Limpiar antes de rellenar

    lineas.forEach(linea => {
        agregarLinea(linea);
    });
}

function obtenerLineasDesdeTabla() {
    const filas = document.querySelectorAll("#lineas-factura tr");
    return Array.from(filas).map(tr => ({
        description: tr.querySelector(".descripcion-linea").value.trim(),
        quantity: parseFloat(tr.querySelector(".cantidad-linea").value),
        unitPrice: parseFloat(tr.querySelector(".precio-linea").value)
    }));
}



function configurarBotones(estado, invoiceId) {
    const btnEmitir = document.getElementById("btnEmitir");
    const btnPDF = document.getElementById("btnPDF");

    btnEmitir.disabled = estado !== "DRAFT";
    btnPDF.disabled = !(estado === "SENT" || estado === "PAID");

    document.getElementById("btnGuardar").onclick = () => guardarFactura(invoiceId);
    btnEmitir.onclick = () => emitirFactura(invoiceId);
    btnPDF.onclick = () => window.open(`/billing/invoices/${invoiceId}/pdf`, "_blank");
}

function guardarFactura(invoiceId) {
    const token = localStorage.getItem("jwt");

    const invoiceLines = obtenerLineasDesdeTabla();
    const description = document.getElementById("descripcion").value.trim();
    const date = document.getElementById("fecha").value;

    if (!description || !date || invoiceLines.length === 0) {
        alert("❗ Debes completar los campos requeridos y añadir al menos una línea de factura.");
        return;
    }

    const facturaActualizada = {
        clientId: currentClientId,
        description,
        date,
        status: document.getElementById("estado").value,
        invoiceLines
    };

    fetch(`/billing/invoices/${invoiceId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(facturaActualizada)
    })
        .then(res => {
            if (!res.ok) throw new Error("Error al guardar");
            alert("✅ Factura actualizada");
            location.reload();
        })
        .catch(err => {
            console.error("❌", err);
            alert("❌ Error al guardar factura");
        });
}



function emitirFactura(invoiceId) {
    const token = localStorage.getItem("jwt");

    fetch(`/billing/invoices/${invoiceId}/emit`, {
        method: "PUT",
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => {
            if (!res.ok) throw new Error("No se pudo emitir");
            alert("📤 Factura emitida");
            location.reload();
        })
        .catch(err => {
            console.error("❌", err);
            alert("❌ Error al emitir factura");
        });
}

function configurarBotones(estado, invoiceId) {
    const btnGuardar = document.getElementById("btnGuardar");
    const btnEmitir = document.getElementById("btnEmitir");
    const btnPDF = document.getElementById("btnPDF");
    const btnPagar = document.getElementById("btnPagar");

    // Estado DRAFT
    if (estado === "DRAFT") {
        btnGuardar.disabled = false;
        btnEmitir.disabled = false;
        btnPDF.disabled = true;
        btnPagar.disabled = true;
    }

    // Estado SENT
    else if (estado === "SENT") {
        btnGuardar.disabled = true;
        btnEmitir.disabled = true;
        btnPDF.disabled = false;
        btnPagar.disabled = false;
        bloquearInputs();
    }

    // Estado PAID
    else if (estado === "PAID") {
        btnGuardar.disabled = true;
        btnEmitir.disabled = true;
        btnPDF.disabled = false;
        btnPagar.disabled = true;
        bloquearInputs();
    }

    // Estado CANCELLED
    else if (estado === "CANCELLED") {
        btnGuardar.disabled = false;
        btnEmitir.disabled = true;
        btnPDF.disabled = true;
        btnPagar.disabled = true;
    }

    btnGuardar.onclick = () => guardarFactura(invoiceId);
    btnEmitir.onclick = () => emitirFactura(invoiceId);
    btnPDF.onclick = () => descargarPDF(invoiceId);
    btnPagar.onclick = () => pagarFactura(invoiceId);
    btnCancelar.onclick = () => cancelarFactura(invoiceId);

}


function pagarFactura(invoiceId) {
    const token = localStorage.getItem("jwt");

    fetch(`/billing/invoices/${invoiceId}/pay`, {
        method: "PUT",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(res => {
        if (!res.ok) {
            return res.text().then(body => {
                throw new Error(body || `Error HTTP ${res.status}`);
            });
        }
        return res.json(); // ✅ Sabemos que viene un InvoiceDTO
    })
    .then(invoice => {
        alert("✅ Factura marcada como pagada (Estado: " + invoice.status + ")");
        location.reload();
    })
    .catch(err => {
        console.error("❌ Error al marcar como pagada:", err);
        alert("❌ No se pudo marcar como pagada. Detalles: " + err.message);
    });
}



function cancelarFactura(invoiceId) {
    const token = localStorage.getItem("jwt");

    if (!confirm("⚠️ ¿Estás seguro de que deseas cancelar esta factura? Esta acción es irreversible.")) {
        return;
    }

    fetch(`/billing/invoices/${invoiceId}`, {
        method: "DELETE",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
        .then(res => {
            if (res.status === 204) {
                alert("🗑 Factura cancelada correctamente.");
                // ✅ Redirigir a la lista de facturas del cliente o general
                window.location.href = "/facturas"; // o `/clientes/1/facturas` si tienes esa vista
            } else {
                return res.json().then(body => {
                    throw new Error(body.message || "Error al cancelar");
                });
            }
        })
        .catch(err => {
            console.error("❌ Error al cancelar factura:", err);
            alert("❌ No se pudo cancelar la factura. Detalles: " + err.message);
        });
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


function bloquearInputs() {
    document.getElementById("descripcion").readOnly = true;
    document.getElementById("fecha").readOnly = true;
    document.getElementById("estado").disabled = true;

    document.querySelectorAll(".descripcion-linea, .cantidad-linea, .precio-linea").forEach(input => {
        input.readOnly = true;
    });
     // Desactivar botón agregar línea
        const btnAgregar = document.getElementById("btnAgregarLinea");
        if (btnAgregar) btnAgregar.disabled = true;

    document.querySelectorAll("button.btn-danger").forEach(btn => btn.disabled = true);
}


