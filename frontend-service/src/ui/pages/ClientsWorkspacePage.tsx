import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { DEFAULT_PAGE_SIZE, loadClientsPage } from "@/application/use-cases/loadClients";
import type { Client } from "@/domain/clients/Client";
import { container } from "@/infrastructure/container";
import { DataTable } from "@/ui/components/DataTable";
import { PaginationControls } from "@/ui/components/PaginationControls";

export function ClientsWorkspacePage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(DEFAULT_PAGE_SIZE);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = container.tokenStorage.getToken();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    loadClientsPage(token, container.billingApi, { page, size: pageSize })
      .then((result) => {
        setClients(result.items);
        setTotalElements(result.totalElements);
        setTotalPages(result.totalPages);
      })
      .catch((err: Error) => setError(err.message));
  }, [page, pageSize]);

  const filteredClients = useMemo(() => {
    const normalized = search.trim().toLowerCase();

    if (!normalized) {
      return clients;
    }

    return clients.filter((client) =>
      [client.name, client.email, client.nif].some((value) => value.toLowerCase().includes(normalized))
    );
  }, [clients, search]);

  return (
    <section className="page-section">
      <div className="section-heading split-heading">
        <div>
          <p className="eyebrow">Billing</p>
          <h3>Clientes</h3>
        </div>
        <Link className="primary-button inline-button" to="/clientes/nuevo">
          Nuevo cliente
        </Link>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <label className="search-field">
        Buscar cliente
        <input
          placeholder="Nombre, email o NIF de esta pagina"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </label>

      <DataTable
        columns={[
          { key: "name", title: "Nombre", render: (client) => client.name },
          { key: "email", title: "Email", render: (client) => client.email },
          { key: "phone", title: "Teléfono", render: (client) => client.phone },
          { key: "nif", title: "NIF", render: (client) => client.nif },
          {
            key: "actions",
            title: "Acciones",
            render: (client) => (
              <div className="table-actions">
                <Link className="table-link" to={`/clientes/${client.clientId}`}>
                  Ver
                </Link>
                <Link className="table-link" to={`/facturas/nueva/${client.clientId}`}>
                  Facturar
                </Link>
              </div>
            )
          }
        ]}
        rows={filteredClients}
        emptyMessage="No hay clientes cargados"
        rowKey={(client) => client.clientId}
      />

      <PaginationControls
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={pageSize}
        itemLabel="clientes"
        onPageChange={setPage}
      />
    </section>
  );
}
