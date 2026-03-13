document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("jwt");
    if (!token) {
        return window.location.href = "/login";
    }

    try {
        const [clientesRes, facturasRes] = await Promise.all([
            fetch(buildGatewayUrl("/billing/clients"), {
                headers: { "Authorization": `Bearer ${token}` }
            }),
            fetch(buildGatewayUrl("/billing/invoices"), {
                headers: { "Authorization": `Bearer ${token}` }
            })
        ]);

        if (!clientesRes.ok || !facturasRes.ok) throw new Error("Error al cargar datos");

        const clientes = await clientesRes.json();
        const facturas = await facturasRes.json();

        const totalFacturado = facturas.reduce((suma, f) => suma + (f.total || 0), 0);

        document.getElementById("total-clientes").innerText = clientes.length;
        document.getElementById("total-facturas").innerText = facturas.length;
        document.getElementById("total-euros").innerText = totalFacturado.toFixed(2) + " €";

    } catch (err) {
        console.error("❌ Error en dashboard:", err);
        alert("No se pudieron cargar los datos del resumen.");
    }
});
