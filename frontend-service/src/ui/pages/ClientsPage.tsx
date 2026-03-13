import { useEffect, useState } from "react";
import { loadClients } from "@/application/use-cases/loadClients";
import type { Client } from "@/domain/clients/Client";
import { container } from "@/infrastructure/container";
import { DataTable } from "@/ui/components/DataTable";

export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = container.tokenStorage.getToken();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    loadClients(token, container.billingApi)
      .then(setClients)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <section className="page-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Billing</p>
          <h3>Clientes</h3>
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <DataTable
        columns={[
          { key: "name", title: "Nombre", render: (client) => client.name },
          { key: "email", title: "Email", render: (client) => client.email },
          { key: "phone", title: "Teléfono", render: (client) => client.phone },
          { key: "nif", title: "NIF", render: (client) => client.nif }
        ]}
        rows={clients}
        emptyMessage="No hay clientes cargados"
      />
    </section>
  );
}
