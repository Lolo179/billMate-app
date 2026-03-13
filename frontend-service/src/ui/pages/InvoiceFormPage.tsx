import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { deleteInvoice } from "@/application/use-cases/deleteInvoice";
import { downloadInvoicePdf } from "@/application/use-cases/downloadInvoicePdf";
import { emitInvoice } from "@/application/use-cases/emitInvoice";
import { loadClient } from "@/application/use-cases/loadClient";
import { loadInvoice } from "@/application/use-cases/loadInvoice";
import { payInvoice } from "@/application/use-cases/payInvoice";
import { saveInvoice } from "@/application/use-cases/saveInvoice";
import type { SaveInvoiceCommand } from "@/application/ports/BillingApiPort";
import type { Client } from "@/domain/clients/Client";
import type { Invoice, InvoiceLine, InvoiceStatus } from "@/domain/invoices/Invoice";
import { container } from "@/infrastructure/container";
import { InvoiceStatusBadge } from "@/ui/components/InvoiceStatusBadge";

function createEmptyLine(): InvoiceLine {
  return {
    description: "",
    quantity: 1,
    unitPrice: 0
  };
}

function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = fileName;
  anchor.click();
  URL.revokeObjectURL(url);
}

export function InvoiceFormPage() {
  const navigate = useNavigate();
  const params = useParams<{ clientId?: string; invoiceId?: string }>();
  const clientIdFromRoute = params.clientId ? Number(params.clientId) : undefined;
  const invoiceId = params.invoiceId ? Number(params.invoiceId) : undefined;
  const token = container.tokenStorage.getToken();

  const [client, setClient] = useState<Client | null>(null);
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [form, setForm] = useState<SaveInvoiceCommand>({
    clientId: clientIdFromRoute ?? 0,
    description: "",
    date: new Date().toISOString().split("T")[0],
    status: "DRAFT",
    invoiceLines: [createEmptyLine()]
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [pendingAction, setPendingAction] = useState<"emit" | "pay" | "download" | "delete" | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const feedbackRef = useRef<HTMLParagraphElement | null>(null);

  useEffect(() => {
    if (!token) {
      setError("No hay sesión activa");
      setLoading(false);
      return;
    }
    const sessionToken = token;

    async function loadPage() {
      try {
        setLoading(true);
        setError(null);

        if (invoiceId) {
          const currentInvoice = await loadInvoice(invoiceId, sessionToken, container.billingApi);
          const currentClient = await loadClient(currentInvoice.clientId, sessionToken, container.billingApi);
          setInvoice(currentInvoice);
          setClient(currentClient);
          setForm({
            clientId: currentInvoice.clientId,
            description: currentInvoice.description,
            date: currentInvoice.date,
            status: currentInvoice.status,
            invoiceLines:
              currentInvoice.invoiceLines && currentInvoice.invoiceLines.length > 0
                ? currentInvoice.invoiceLines.map((line) => ({
                    description: line.description,
                    quantity: line.quantity,
                    unitPrice: line.unitPrice
                  }))
                : [createEmptyLine()]
          });
          return;
        }

        if (!clientIdFromRoute) {
          throw new Error("Cliente no válido para crear la factura");
        }

        const currentClient = await loadClient(clientIdFromRoute, sessionToken, container.billingApi);
        setClient(currentClient);
        setForm((current) => ({ ...current, clientId: currentClient.clientId }));
      } catch (err) {
        setError(err instanceof Error ? err.message : "No se pudo cargar la factura");
      } finally {
        setLoading(false);
      }
    }

    loadPage();
  }, [clientIdFromRoute, invoiceId, token]);

  const subtotal = useMemo(
    () =>
      form.invoiceLines.reduce(
        (sum, line) => sum + (Number(line.quantity) || 0) * (Number(line.unitPrice) || 0),
        0
      ),
    [form.invoiceLines]
  );
  const vat = subtotal * 0.21;
  const total = subtotal + vat;

  const currentStatus: InvoiceStatus = invoice?.status ?? "DRAFT";
  const isEditable = !invoiceId || currentStatus === "DRAFT" || currentStatus === "CANCELLED";
  const isBusy = loading || saving || pendingAction !== null;
  const statusNote = useMemo(() => {
    if (!invoiceId) {
      return "La factura se crea en borrador para que puedas revisarla antes de emitirla.";
    }

    switch (currentStatus) {
      case "DRAFT":
        return "Puedes editar todos los campos antes de emitir la factura.";
      case "SENT":
        return "La factura ya fue emitida. Solo queda marcarla como pagada o descargar el PDF.";
      case "PAID":
        return "La factura ya esta cerrada como pagada. Solo puedes consultarla o descargar el PDF.";
      case "CANCELLED":
        return "La factura esta cancelada. Puedes corregirla antes de volver a emitirla.";
      default:
        return "";
    }
  }, [currentStatus, invoiceId]);

  useEffect(() => {
    if (error || notice) {
      feedbackRef.current?.focus();
    }
  }, [error, notice]);

  function updateLine(index: number, field: keyof InvoiceLine, value: string) {
    setForm((current) => ({
      ...current,
      invoiceLines: current.invoiceLines.map((line, lineIndex) =>
        lineIndex === index
          ? {
              ...line,
              [field]: field === "description" ? value : Number(value)
            }
          : line
      )
    }));
  }

  function addLine() {
    if (isBusy) {
      return;
    }
    setForm((current) => ({ ...current, invoiceLines: [...current.invoiceLines, createEmptyLine()] }));
  }

  function removeLine(index: number) {
    if (isBusy) {
      return;
    }
    setForm((current) => ({
      ...current,
      invoiceLines:
        current.invoiceLines.length === 1
          ? [createEmptyLine()]
          : current.invoiceLines.filter((_, lineIndex) => lineIndex !== index)
    }));
  }

  async function handleSave(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setNotice(null);
      const savedInvoice = await saveInvoice(form, token, container.billingApi, invoiceId);
      setNotice(invoiceId ? "Factura actualizada correctamente" : "Factura creada correctamente");

      if (!invoiceId) {
        navigate(`/facturas/${savedInvoice.invoiceId}/editar`);
        return;
      }

      setInvoice(savedInvoice);
      setForm((current) => ({ ...current, status: savedInvoice.status }));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo guardar la factura");
    } finally {
      setSaving(false);
    }
  }

  async function handleEmit() {
    if (!invoiceId || !token) {
      return;
    }

    try {
      setPendingAction("emit");
      setError(null);
      setNotice(null);
      const pdf = await emitInvoice(invoiceId, token, container.billingApi);
      downloadBlob(pdf, `factura_${invoiceId}.pdf`);
      setNotice("Factura emitida y PDF generado");
      const refreshed = await loadInvoice(invoiceId, token, container.billingApi);
      setInvoice(refreshed);
      setForm((current) => ({ ...current, status: refreshed.status }));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo emitir la factura");
    } finally {
      setPendingAction(null);
    }
  }

  async function handlePay() {
    if (!invoiceId || !token) {
      return;
    }

    try {
      setPendingAction("pay");
      setError(null);
      setNotice(null);
      const paid = await payInvoice(invoiceId, token, container.billingApi);
      setInvoice(paid);
      setForm((current) => ({ ...current, status: paid.status }));
      setNotice("Factura marcada como pagada");
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo marcar como pagada");
    } finally {
      setPendingAction(null);
    }
  }

  async function handleDownloadPdf() {
    if (!invoiceId || !token) {
      return;
    }

    try {
      setPendingAction("download");
      setError(null);
      setNotice(null);
      const pdf = await downloadInvoicePdf(invoiceId, token, container.billingApi);
      downloadBlob(pdf, `factura_${invoiceId}.pdf`);
      setNotice("PDF descargado correctamente");
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo descargar el PDF");
    } finally {
      setPendingAction(null);
    }
  }

  async function handleDelete() {
    if (!invoiceId || !token || !window.confirm("¿Eliminar esta factura?")) {
      return;
    }

    try {
      setPendingAction("delete");
      setError(null);
      setNotice(null);
      await deleteInvoice(invoiceId, token, container.billingApi);
      navigate(client ? `/clientes/${client.clientId}/facturas` : "/facturas");
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo eliminar la factura");
    } finally {
      setPendingAction(null);
    }
  }

  return (
    <section className="page-section">
      <div className="section-heading split-heading">
        <div>
          <p className="eyebrow">Facturas</p>
          <h3>{invoiceId ? `Editar factura #${invoiceId}` : "Nueva factura"}</h3>
        </div>
        <div className="button-row">
          <Link className="secondary-button inline-button" to={client ? `/clientes/${client.clientId}` : "/facturas"}>
            Volver
          </Link>
        </div>
      </div>

      {error ? (
        <p ref={feedbackRef} className="error-banner" role="alert" tabIndex={-1}>
          {error}
        </p>
      ) : null}
      {notice ? (
        <p ref={feedbackRef} className="success-banner" role="status" tabIndex={-1}>
          {notice}
        </p>
      ) : null}

      {loading ? (
        <p className="muted-copy">Cargando factura...</p>
      ) : (
        <form className="stack-form invoice-layout" onSubmit={handleSave} aria-busy={isBusy}>
          <div className="detail-card detail-card-wide">
            <span>Cliente</span>
            <strong>{client ? `${client.name} · ${client.email}` : "Sin cliente"}</strong>
          </div>

          <p className="form-hint">{statusNote}</p>

          <div className="split-fields">
            <label>
              Descripción
              <input
                value={form.description}
                onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
                disabled={!isEditable || isBusy}
              />
            </label>
            <label>
              Fecha
              <input
                type="date"
                value={form.date}
                onChange={(event) => setForm((current) => ({ ...current, date: event.target.value }))}
                disabled={!isEditable || isBusy}
              />
            </label>
          </div>

          {invoiceId ? (
            <label>
              Estado
              <select
                value={form.status}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    status: event.target.value as InvoiceStatus
                  }))
                }
                disabled={!isEditable || isBusy}
              >
                <option value="DRAFT">Borrador</option>
                <option value="SENT">Emitida</option>
                <option value="PAID">Pagada</option>
                <option value="CANCELLED">Cancelada</option>
              </select>
            </label>
          ) : null}

          <div className="section-heading compact-heading">
            <div>
              <p className="eyebrow">Líneas</p>
              <h4>Detalle de factura</h4>
            </div>
            <button
              className="secondary-button inline-button"
              type="button"
              onClick={addLine}
              disabled={!isEditable || isBusy}
            >
              Añadir línea
            </button>
          </div>

          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Descripción</th>
                  <th>Cantidad</th>
                  <th>Precio</th>
                  <th>Total</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {form.invoiceLines.map((line, index) => {
                  const lineTotal = (Number(line.quantity) || 0) * (Number(line.unitPrice) || 0);

                  return (
                    <tr key={`${index}-${line.description}`}>
                      <td>
                        <input
                          value={line.description}
                          onChange={(event) => updateLine(index, "description", event.target.value)}
                          disabled={!isEditable || isBusy}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          min="0.01"
                          step="0.01"
                          value={line.quantity}
                          onChange={(event) => updateLine(index, "quantity", event.target.value)}
                          disabled={!isEditable || isBusy}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          min="0.01"
                          step="0.01"
                          value={line.unitPrice}
                          onChange={(event) => updateLine(index, "unitPrice", event.target.value)}
                          disabled={!isEditable || isBusy}
                        />
                      </td>
                      <td>{lineTotal.toFixed(2)} €</td>
                      <td>
                        <button
                          className="danger-button inline-button"
                          type="button"
                          onClick={() => removeLine(index)}
                          disabled={!isEditable || isBusy}
                        >
                          Quitar
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="totals-panel">
            <p>
              <span>Base imponible</span>
              <strong>{subtotal.toFixed(2)} €</strong>
            </p>
            <p>
              <span>IVA (21%)</span>
              <strong>{vat.toFixed(2)} €</strong>
            </p>
            <p className="totals-emphasis">
              <span>Total</span>
              <strong>{total.toFixed(2)} €</strong>
            </p>
          </div>

          <div className="actions-card">
            <div className="actions-card-header">
              <div>
                <h4>Acciones de la factura</h4>
                <p>Las acciones disponibles cambian segun el estado actual de la factura.</p>
              </div>
              {invoiceId ? <InvoiceStatusBadge status={currentStatus} /> : null}
            </div>

            <div className="action-stack">
              <div className="action-group">
                <button className="primary-button inline-button" type="submit" disabled={isBusy || !isEditable}>
                  {saving ? "Guardando..." : invoiceId ? "Guardar cambios" : "Crear factura"}
                </button>
                {invoiceId && currentStatus === "DRAFT" ? (
                  <button
                    className="secondary-button inline-button"
                    type="button"
                    onClick={handleEmit}
                    disabled={isBusy}
                  >
                    {pendingAction === "emit" ? "Emitiendo..." : "Emitir"}
                  </button>
                ) : null}
                {invoiceId && currentStatus === "SENT" ? (
                  <button
                    className="secondary-button inline-button"
                    type="button"
                    onClick={handlePay}
                    disabled={isBusy}
                  >
                    {pendingAction === "pay" ? "Actualizando..." : "Marcar pagada"}
                  </button>
                ) : null}
                {invoiceId && (currentStatus === "SENT" || currentStatus === "PAID") ? (
                  <button
                    className="secondary-button inline-button"
                    type="button"
                    onClick={handleDownloadPdf}
                    disabled={isBusy}
                  >
                    {pendingAction === "download" ? "Descargando..." : "Descargar PDF"}
                  </button>
                ) : null}
              </div>

              {invoiceId ? (
                <div className="danger-zone">
                  <div>
                    <strong>Zona delicada</strong>
                    <p>Eliminar esta factura la quita del historial del cliente de forma inmediata.</p>
                  </div>
                  <button className="danger-button inline-button" type="button" onClick={handleDelete} disabled={isBusy}>
                    {pendingAction === "delete" ? "Eliminando..." : "Eliminar factura"}
                  </button>
                </div>
              ) : null}
            </div>
          </div>
        </form>
      )}
    </section>
  );
}
