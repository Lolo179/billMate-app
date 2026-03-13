import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { deleteClient } from "@/application/use-cases/deleteClient";
import { loadClient } from "@/application/use-cases/loadClient";
import { loadInvoicesByClient } from "@/application/use-cases/loadInvoicesByClient";
import type { Client } from "@/domain/clients/Client";
import type { Invoice } from "@/domain/invoices/Invoice";
import { container } from "@/infrastructure/container";
import { InvoiceStatusBadge } from "@/ui/components/InvoiceStatusBadge";

export function ClientDetailPage() {
  const navigate = useNavigate();
  const params = useParams<{ clientId: string }>();
  const clientId = Number(params.clientId);
  const token = container.tokenStorage.getToken();

  const [client, setClient] = useState<Client | null>(null);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token || Number.isNaN(clientId)) {
      setError("Cliente no válido o sesión inexistente");
      return;
    }

    Promise.all([
      loadClient(clientId, token, container.billingApi),
      loadInvoicesByClient(clientId, token, container.billingApi)
    ])
      .then(([clientResult, invoicesResult]) => {
        setClient(clientResult);
        setInvoices(invoicesResult);
      })
      .catch((err: Error) => setError(err.message));
  }, [clientId, token]);

  async function handleDelete() {
    if (!token || !client || !window.confirm(`¿Eliminar al cliente ${client.name}?`)) {
      return;
    }

    try {
      setDeleting(true);
      setError(null);
      await deleteClient(client.clientId, token, container.billingApi);
      navigate("/clientes");
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo eliminar el cliente");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <section className="page-section">
      <div className="section-heading split-heading">
        <div>
          <p className="eyebrow">Cliente</p>
          <h3>{client?.name ?? "Detalle de cliente"}</h3>
        </div>
        <div className="button-row">
          <Link className="secondary-button inline-button" to="/clientes">
            Volver
          </Link>
          {client ? (
            <>
              <Link className="secondary-button inline-button" to={`/clientes/${client.clientId}/editar`}>
                Editar
              </Link>
              <Link className="primary-button inline-button" to={`/facturas/nueva/${client.clientId}`}>
                Nueva factura
              </Link>
            </>
          ) : null}
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      {client ? (
        <>
          <div className="details-grid">
            <article className="detail-card">
              <span>Email</span>
              <strong>{client.email}</strong>
            </article>
            <article className="detail-card">
              <span>Teléfono</span>
              <strong>{client.phone}</strong>
            </article>
            <article className="detail-card">
              <span>NIF</span>
              <strong>{client.nif}</strong>
            </article>
            <article className="detail-card detail-card-wide">
              <span>Dirección</span>
              <strong>{client.address}</strong>
            </article>
          </div>

          <div className="section-heading compact-heading">
            <div>
              <p className="eyebrow">Facturas</p>
              <h4>Actividad reciente</h4>
              <p className="section-copy">Gestiona las facturas del cliente sin salir de su ficha.</p>
            </div>
            <div className="action-stack">
              <div className="action-group">
                <Link className="secondary-button inline-button" to={`/clientes/${client.clientId}/facturas`}>
                  Ver todas
                </Link>
              </div>
              <div className="danger-zone">
                <div>
                  <strong>Zona delicada</strong>
                  <p>Eliminar el cliente tambien borrara sus facturas asociadas.</p>
                </div>
                <button className="danger-button inline-button" onClick={handleDelete} type="button" disabled={deleting}>
                  {deleting ? "Eliminando..." : "Eliminar cliente"}
                </button>
              </div>
            </div>
          </div>

          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Fecha</th>
                  <th>Descripción</th>
                  <th>Total</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {invoices.length === 0 ? (
                  <tr>
                    <td colSpan={6}>No hay facturas asociadas</td>
                  </tr>
                ) : (
                  invoices.slice(0, 5).map((invoice) => (
                    <tr key={invoice.invoiceId}>
                      <td>{invoice.invoiceId}</td>
                      <td>{invoice.date}</td>
                      <td>{invoice.description}</td>
                      <td>{invoice.total.toFixed(2)} €</td>
                      <td>
                        <InvoiceStatusBadge status={invoice.status} />
                      </td>
                      <td>
                        <Link className="table-link" to={`/facturas/${invoice.invoiceId}/editar`}>
                          Abrir
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </>
      ) : (
        <p className="muted-copy">Cargando cliente...</p>
      )}
    </section>
  );
}
