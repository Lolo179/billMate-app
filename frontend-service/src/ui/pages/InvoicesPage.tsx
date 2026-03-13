import { useEffect, useState } from "react";
import { loadInvoices } from "@/application/use-cases/loadInvoices";
import type { Invoice } from "@/domain/invoices/Invoice";
import { container } from "@/infrastructure/container";
import { DataTable } from "@/ui/components/DataTable";

export function InvoicesPage() {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = container.tokenStorage.getToken();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    loadInvoices(token, container.billingApi)
      .then(setInvoices)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <section className="page-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Billing</p>
          <h3>Facturas</h3>
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <DataTable
        columns={[
          { key: "invoiceId", title: "ID", render: (invoice) => invoice.invoiceId },
          { key: "clientName", title: "Cliente", render: (invoice) => invoice.clientName },
          { key: "date", title: "Fecha", render: (invoice) => invoice.date },
          { key: "total", title: "Total", render: (invoice) => `${invoice.total.toFixed(2)} €` },
          { key: "status", title: "Estado", render: (invoice) => invoice.status }
        ]}
        rows={invoices}
        emptyMessage="No hay facturas cargadas"
      />
    </section>
  );
}
