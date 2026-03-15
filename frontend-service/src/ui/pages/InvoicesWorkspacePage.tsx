import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { loadClient } from "@/application/use-cases/loadClient";
import { DEFAULT_PAGE_SIZE, loadClients } from "@/application/use-cases/loadClients";
import { loadInvoicesPage } from "@/application/use-cases/loadInvoices";
import { loadInvoicesByClientPage } from "@/application/use-cases/loadInvoicesByClient";
import type { Client } from "@/domain/clients/Client";
import type { Invoice } from "@/domain/invoices/Invoice";
import { container } from "@/infrastructure/container";
import { DataTable } from "@/ui/components/DataTable";
import { InvoiceStatusBadge } from "@/ui/components/InvoiceStatusBadge";
import { PaginationControls } from "@/ui/components/PaginationControls";

export function InvoicesWorkspacePage() {
  const params = useParams<{ clientId?: string }>();
  const clientId = params.clientId ? Number(params.clientId) : undefined;
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [client, setClient] = useState<Client | null>(null);
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

    const invoicesPromise = clientId
      ? loadInvoicesByClientPage(clientId, token, container.billingApi, { page, size: pageSize })
      : loadInvoicesPage(token, container.billingApi, { page, size: pageSize });
    const clientsPromise = loadClients(token, container.billingApi);
    const clientPromise = clientId
      ? loadClient(clientId, token, container.billingApi).then((result) => setClient(result))
      : Promise.resolve();

    Promise.all([invoicesPromise, clientsPromise, clientPromise])
      .then(([invoicePage, clientList]) => {
        const clientNames = new Map(clientList.map((item) => [item.clientId, item.name]));
        setTotalElements(invoicePage.totalElements);
        setTotalPages(invoicePage.totalPages);
        setInvoices(
          invoicePage.items.map((invoice) => ({
            ...invoice,
            clientName: invoice.clientName ?? clientNames.get(invoice.clientId)
          }))
        );
      })
      .catch((err: Error) => setError(err.message));
  }, [clientId, page, pageSize]);

  const filteredInvoices = useMemo(() => {
    const normalized = search.trim().toLowerCase();

    if (!normalized) {
      return invoices;
    }

    return invoices.filter((invoice) =>
      [String(invoice.invoiceId), invoice.description, invoice.status].some((value) =>
        value.toLowerCase().includes(normalized)
      )
    );
  }, [invoices, search]);

  return (
    <section className="page-section">
      <div className="section-heading split-heading">
        <div>
          <p className="eyebrow">Billing</p>
          <h3>{client ? `Facturas de ${client.name}` : "Facturas"}</h3>
        </div>
        {client ? (
          <div className="button-row">
            <Link className="secondary-button inline-button" to={`/clientes/${client.clientId}`}>
              Ver cliente
            </Link>
            <Link className="primary-button inline-button" to={`/facturas/nueva/${client.clientId}`}>
              Nueva factura
            </Link>
          </div>
        ) : null}
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <label className="search-field">
        Buscar factura
        <input
          placeholder="ID, descripcion o estado de esta pagina"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </label>

      <DataTable
        columns={[
          { key: "invoiceId", title: "ID", render: (invoice) => invoice.invoiceId },
          {
            key: "clientId",
            title: "Cliente",
            render: (invoice) => invoice.clientName ?? `Cliente #${invoice.clientId}`
          },
          { key: "date", title: "Fecha", render: (invoice) => invoice.date },
          { key: "total", title: "Total", render: (invoice) => `${invoice.total.toFixed(2)} €` },
          { key: "status", title: "Estado", render: (invoice) => <InvoiceStatusBadge status={invoice.status} /> },
          {
            key: "actions",
            title: "Acciones",
            render: (invoice) => (
              <Link className="table-link" to={`/facturas/${invoice.invoiceId}/editar`}>
                Abrir
              </Link>
            )
          }
        ]}
        rows={filteredInvoices}
        emptyMessage="No hay facturas cargadas"
        rowKey={(invoice) => invoice.invoiceId}
      />

      <PaginationControls
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={pageSize}
        itemLabel="facturas"
        onPageChange={setPage}
      />
    </section>
  );
}
