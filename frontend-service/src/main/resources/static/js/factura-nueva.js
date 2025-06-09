document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");
    const clientId = window.location.pathname.split("/").pop();

    if (!token || !clientId) {
        window.location.href = "/login";
        return;
    }

    // Cargar info del cliente
    fetch(`/billing/clients/${clientId}`, {
        headers: { "Authorization": "Bearer " + token }
    })
    .then(res => res.json())
    .then(cliente => {
        document.getElementById("clienteInfo").value = `${cliente.name} (${cliente.email})`;
    })
    .catch(() => {
        document.getElementById("clienteInfo").value = "❌ Error al cargar cliente";
    });

    // Fecha actual por defecto
    document.getElementById("fecha").value = new Date().toISOString().split("T")[0];
});

// Guardar nueva factura
function guardarNuevaFactura() {
    const token = localStorage.getItem("jwt");
    const clientId = window.location.pathname.split("/").pop();

    const nuevaFactura = {
        clientId: parseInt(clientId),
        description: document.getElementById("descripcion").value.trim(),
        date: document.getElementById("fecha").value,
        invoiceLines: obtenerLineasDesdeTabla(),
        status: "DRAFT"
    };

    // Validación
    if (!nuevaFactura.description || !nuevaFactura.date) {
        alert("❗ Todos los campos son obligatorios.");
        return;
    }
    if (nuevaFactura.invoiceLines.length === 0) {
        alert("❗ Debes agregar al menos una línea.");
        return;
    }

    fetch("/billing/invoices", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(nuevaFactura)
    })
    .then(res => {
        if (!res.ok) throw new Error("Error al crear la factura");
        return res.json();
    })
    .then(data => {
        alert("✅ Factura creada correctamente.");
        window.location.href = `/facturas/${data.invoiceId}/editar`;
    })
    .catch(err => {
        console.error("❌ Error al crear factura:", err);
        alert("❌ No se pudo crear la factura.");
    });
}

function obtenerLineasDesdeTabla() {
    const filas = document.querySelectorAll("#lineas-factura tr");
    return Array.from(filas).map(tr => ({
        description: tr.querySelector(".descripcion-linea").value.trim(),
        quantity: parseFloat(tr.querySelector(".cantidad-linea").value),
        unitPrice: parseFloat(tr.querySelector(".precio-linea").value)
    })).filter(linea => linea.description && linea.quantity > 0 && linea.unitPrice > 0);
}

function agregarLinea(linea = {}) {
    const tbody = document.getElementById("lineas-factura");
    const tr = document.createElement("tr");

    tr.innerHTML = `
        <td><input class="form-control descripcion-linea" value="${linea.description || ''}"></td>
        <td><input type="number" class="form-control cantidad-linea" value="${linea.quantity || 1}" min="0.01" step="0.01"></td>
        <td><input type="number" class="form-control precio-linea" value="${linea.unitPrice || 0}" step="0.01" min="0.01"></td>
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
    tbody.innerHTML = "";
    lineas.forEach(agregarLinea);
}

document.getElementById("btnCancelar").addEventListener("click", () => {
    const clientId = window.location.pathname.split("/").pop();
    window.location.href = `/clientes/${clientId}/facturas`;
});

