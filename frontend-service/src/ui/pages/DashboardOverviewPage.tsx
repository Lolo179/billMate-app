import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { loadDashboardSummary, type DashboardSummary } from "@/application/use-cases/loadDashboardSummary";
import { loadInvoices } from "@/application/use-cases/loadInvoices";
import type { Invoice } from "@/domain/invoices/Invoice";
import { container } from "@/infrastructure/container";
import { InfoCard } from "@/ui/components/InfoCard";
import { InvoiceStatusBadge } from "@/ui/components/InvoiceStatusBadge";

export function DashboardOverviewPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [recentInvoices, setRecentInvoices] = useState<Invoice[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = container.tokenStorage.getToken();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    Promise.all([
      loadDashboardSummary(token, container.billingApi),
      loadInvoices(token, container.billingApi)
    ])
      .then(([summaryResult, invoices]) => {
        setSummary(summaryResult);
        setRecentInvoices(invoices);
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  const statusBreakdown = useMemo(() => {
    return recentInvoices.reduce<Record<string, number>>((acc, invoice) => {
      acc[invoice.status] = (acc[invoice.status] ?? 0) + 1;
      return acc;
    }, {});
  }, [recentInvoices]);

  return (
    <section className="page-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Resumen</p>
          <h3>Panel operativo</h3>
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <div className="card-grid">
        <InfoCard title="Clientes" value={String(summary?.clientsCount ?? "-")} />
        <InfoCard title="Facturas" value={String(summary?.invoicesCount ?? "-")} />
        <InfoCard
          title="Facturación"
          value={summary ? `${summary.totalInvoiced.toFixed(2)} €` : "-"}
          detail="Agregado desde billing-service a través del gateway"
        />
      </div>

      <div className="details-grid dashboard-grid">
        <article className="detail-card detail-card-wide">
          <div className="section-heading compact-heading">
            <div>
              <p className="eyebrow">Operativa</p>
              <h4>Estados de factura</h4>
            </div>
            <Link className="table-link" to="/facturas">
              Ir a facturas
            </Link>
          </div>
          <div className="status-grid">
            {(["DRAFT", "SENT", "PAID", "CANCELLED"] as const).map((status) => (
              <div key={status} className="status-card">
                <InvoiceStatusBadge status={status} />
                <strong>{statusBreakdown[status] ?? 0}</strong>
              </div>
            ))}
          </div>
        </article>

        <article className="detail-card detail-card-wide">
          <div className="section-heading compact-heading">
            <div>
              <p className="eyebrow">Últimas facturas</p>
              <h4>Seguimiento rápido</h4>
            </div>
          </div>
          <div className="table-wrapper compact-table">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Descripción</th>
                  <th>Total</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {recentInvoices.slice(0, 5).map((invoice) => (
                  <tr key={invoice.invoiceId}>
                    <td>{invoice.invoiceId}</td>
                    <td>{invoice.description}</td>
                    <td>{invoice.total.toFixed(2)} €</td>
                    <td>
                      <InvoiceStatusBadge status={invoice.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>
      </div>
    </section>
  );
}
