function logout() {
    localStorage.removeItem("jwt"); // 🧼 Limpia el token
    window.location.href = "/login"; // 🔁 Redirige al login
}

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");
    if (!token) return;

    const payload = JSON.parse(atob(token.split(".")[1]));
    const roles = payload.roles || [];

    // Mostrar enlace de gestión solo si es ADMIN
    const menu = document.getElementById("gestion-usuarios-menu");
    if (roles.includes("ADMIN") && menu) {
        menu.style.display = "block";
    }

    // Mostrar info usuario en el navbar
    mostrarInfoUsuarioNavbar();
});

function getUserFromToken() {
    const token = localStorage.getItem("jwt");
    if (!token) return null;

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return {
            username: payload.sub,              // O usa 'email' si ese es el claim
            roles: payload.roles || []
        };
    } catch (e) {
        console.error("❌ Token mal formado", e);
        return null;
    }
}

function mostrarInfoUsuarioNavbar() {
    const user = getUserFromToken();
    if (!user) return;

    const nombre = user.username;
    const rol = user.roles.length > 0 ? user.roles[0] : "N/A";

    const span = document.getElementById("user-info");
    if (span) {
        span.textContent = `👤 ${nombre} (${rol})`;
    }
}
