import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { container } from "@/infrastructure/container";

const navigationItems = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/clientes", label: "Clientes" },
  { to: "/facturas", label: "Facturas" },
  { to: "/usuarios", label: "Usuarios" }
];

export function AppShell() {
  const navigate = useNavigate();
  const session = container.tokenStorage.getSession();

  function handleLogout() {
    container.tokenStorage.clear();
    navigate("/login");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">BillMate</p>
          <h1>BillMate</h1>
          <p className="sidebar-copy">Facturación para pequeños negocios.</p>
        </div>
        <nav className="nav-menu">
          {navigationItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button className="secondary-button" onClick={handleLogout}>
          Cerrar sesión
        </button>
      </aside>
      <main className="content">
        <header className="page-header">
          <div>
            <p className="eyebrow">Sesión activa</p>
            <h2>{session?.email ?? "Sin autenticar"}</h2>
          </div>
          <span className="status-chip">{session?.roles.join(", ") ?? "Sin roles"}</span>
        </header>
        <Outlet />
      </main>
    </div>
  );
}
