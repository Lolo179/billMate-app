document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");

    if (!token) {
        alert("⛔ Acceso denegado");
        window.location.href = "/login";
        return;
    }

    const payload = JSON.parse(atob(token.split('.')[1]));
    const roles = payload.roles;

    if (!roles.includes("ADMIN")) {
        alert("⛔ Solo administradores pueden acceder.");
        window.location.href = "/clientes";
        return;
    }

    const btn = document.getElementById("btnAbrirModalNuevoUsuario");
    if (btn && roles.includes("ADMIN")) {
        btn.style.display = "block";
    }

    cargarUsuarios();
});


function cargarUsuarios() {
    const token = localStorage.getItem("jwt");

    fetch("/auth/users", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    })
    .then(res => {
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }
        return res.json();
    })
    .then(data => {
        const tbody = document.querySelector("#tablaUsuarios tbody");
        tbody.innerHTML = "";

        // Actualizar contador si tienes el badge
        const badge = document.getElementById("totalUsuarios");
        if (badge) badge.textContent = data.length;

        data.forEach(usuario => {
            const tr = document.createElement("tr");

            // Crear badges para roles
            const rolesHtml = usuario.roles.map(role => {
                const clase = role === "ADMIN" ? "badge-danger" : "badge-secondary";
                return `<span class="badge ${clase}">${role}</span>`;
            }).join(" ");

            tr.innerHTML = `
                <td>${usuario.id}</td>
                <td>${usuario.username}</td>
                <td>${usuario.email}</td>
                <td>${rolesHtml}</td>
                <td>
                    <button class="btn btn-sm btn-info" onclick="verUsuario(${usuario.id})">👁</button>
                    <button class="btn btn-sm btn-warning" onclick="editarUsuario(${usuario.id})">✏️</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarUsuario(${usuario.id})">🗑</button>
                </td>
            `;

            tbody.appendChild(tr);
        });
    })
    .catch(err => {
        console.error("❌ Error al cargar usuarios", err);
        alert("No se pudieron cargar los usuarios.");
    });
}


function registrarUsuario(event) {
    event.preventDefault();
    const token = localStorage.getItem("jwt");

    const nuevoUsuario = {
        username: document.getElementById("nuevoUsername").value.trim(),
        email: document.getElementById("nuevoEmail").value.trim(),
        password: document.getElementById("nuevoPassword").value,
        role: document.getElementById("nuevoRol").value  // 👈 incluir rol
    };

    fetch("/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(nuevoUsuario)
    })
    .then(res => {
        if (!res.ok) throw new Error("Error al registrar");
        $('#modalNuevoUsuario').modal('hide');
        alert("✅ Usuario registrado");
        cargarUsuarios();
    })
    .catch(err => {
        console.error("❌ Error al registrar usuario", err);
        alert("No se pudo registrar el usuario.");
    });
}

