import { useEffect, useState } from "react";
import { loadUsers } from "@/application/use-cases/loadUsers";
import type { User } from "@/domain/users/User";
import { container } from "@/infrastructure/container";
import { DataTable } from "@/ui/components/DataTable";

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = container.tokenStorage.getToken();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    loadUsers(token, container.authApi)
      .then(setUsers)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <section className="page-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Auth</p>
          <h3>Usuarios</h3>
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <DataTable
        columns={[
          { key: "id", title: "ID", render: (user) => user.id },
          { key: "username", title: "Usuario", render: (user) => user.username },
          { key: "email", title: "Email", render: (user) => user.email },
          { key: "roles", title: "Roles", render: (user) => user.roles.join(", ") }
        ]}
        rows={users}
        emptyMessage="No hay usuarios cargados"
      />
    </section>
  );
}
