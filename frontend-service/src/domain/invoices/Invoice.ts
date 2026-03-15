export type InvoiceStatus = "DRAFT" | "SENT" | "PAID" | "CANCELLED";

export interface InvoiceLine {
  description: string;
  quantity: number;
  unitPrice: number;
  total?: number;
}

export interface Invoice {
  invoiceId: number;
  clientId: number;
  clientName?: string;
  description: string;
  date: string;
  invoiceLines?: InvoiceLine[];
  total: number;
  status: InvoiceStatus;
  createdAt?: string;
  taxPercentage?: number;
}
