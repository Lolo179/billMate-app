import type { Client } from "@/domain/clients/Client";
import type { Invoice, InvoiceLine, InvoiceStatus } from "@/domain/invoices/Invoice";

export interface SaveClientCommand {
  name: string;
  email: string;
  phone: string;
  nif: string;
  address: string;
}

export interface SaveInvoiceCommand {
  clientId: number;
  description: string;
  date: string;
  status?: InvoiceStatus;
  invoiceLines: InvoiceLine[];
}

export interface PageQuery {
  page: number;
  size: number;
}

export interface PageResult<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface BillingApiPort {
  getClients(query: PageQuery, token: string): Promise<PageResult<Client>>;
  getClientById(clientId: number, token: string): Promise<Client>;
  createClient(command: SaveClientCommand, token: string): Promise<Client>;
  updateClient(clientId: number, command: SaveClientCommand, token: string): Promise<Client>;
  deleteClient(clientId: number, token: string): Promise<void>;
  getInvoices(query: PageQuery, token: string): Promise<PageResult<Invoice>>;
  getInvoicesByClientId(clientId: number, query: PageQuery, token: string): Promise<PageResult<Invoice>>;
  getInvoiceById(invoiceId: number, token: string): Promise<Invoice>;
  createInvoice(command: SaveInvoiceCommand, token: string): Promise<Invoice>;
  updateInvoice(invoiceId: number, command: SaveInvoiceCommand, token: string): Promise<Invoice>;
  emitInvoice(invoiceId: number, token: string): Promise<Blob>;
  downloadInvoicePdf(invoiceId: number, token: string): Promise<Blob>;
  payInvoice(invoiceId: number, token: string): Promise<Invoice>;
  deleteInvoice(invoiceId: number, token: string): Promise<void>;
}
