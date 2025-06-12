document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");
    if (!token) {
        window.location.href = "/login";
        return;
    }
});

document.getElementById("btnGuardarCliente").addEventListener("click", () => {
    const token = localStorage.getItem("jwt");

    const nuevoCliente = {
        name: document.getElementById("nombre").value.trim(),
        email: document.getElementById("email").value.trim(),
        phone: document.getElementById("telefono").value.trim(),
        nif: document.getElementById("nif").value.trim().toUpperCase(),
        address: document.getElementById("direccion").value.trim()
    };

    // Validación
    if (!nuevoCliente.name || !nuevoCliente.email || !nuevoCliente.nif || !nuevoCliente.address) {
        alert("❗ Todos los campos obligatorios deben ser rellenados.");
        return;
    }

    fetch("/billing/clients", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(nuevoCliente)
    })
    .then(res => {
        if (!res.ok) throw new Error("Error al crear el cliente");
        return res.json();
    })
    .then(data => {
        alert("✅ Cliente creado con éxito.");
        window.location.href = "/clientes"; // Redirección al listado
    })
    .catch(err => {
        console.error("❌ Error al crear cliente:", err);
        alert("❌ No se pudo crear el cliente.");
    });
});

document.getElementById("btnCancelarCliente").addEventListener("click", () => {
    window.location.href = "/clientes";
});
