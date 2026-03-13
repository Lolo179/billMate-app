import type { InvoiceStatus } from "@/domain/invoices/Invoice";

const statusLabels: Record<InvoiceStatus, string> = {
  DRAFT: "Borrador",
  SENT: "Emitida",
  PAID: "Pagada",
  CANCELLED: "Cancelada"
};

export function InvoiceStatusBadge({ status }: { status: InvoiceStatus }) {
  return <span className={`status-pill status-${status.toLowerCase()}`}>{statusLabels[status]}</span>;
}
