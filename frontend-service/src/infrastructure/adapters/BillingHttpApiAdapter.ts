import type {
  BillingApiPort,
  PageQuery,
  PageResult,
  SaveClientCommand,
  SaveInvoiceCommand
} from "@/application/ports/BillingApiPort";
import type { Client } from "@/domain/clients/Client";
import type { Invoice } from "@/domain/invoices/Invoice";
import { ApiClient } from "@/infrastructure/http/ApiClient";

export class BillingHttpApiAdapter implements BillingApiPort {
  constructor(private readonly apiClient: ApiClient) {}

  async getClients(query: PageQuery, token: string): Promise<PageResult<Client>> {
    return this.apiClient.get<PageResult<Client>>(
      `/billing/clients?page=${query.page}&size=${query.size}`,
      token
    );
  }

  async getClientById(clientId: number, token: string): Promise<Client> {
    return this.apiClient.get<Client>(`/billing/clients/${clientId}`, token);
  }

  async createClient(command: SaveClientCommand, token: string): Promise<Client> {
    return this.apiClient.post<Client>("/billing/clients", command, token);
  }

  async updateClient(clientId: number, command: SaveClientCommand, token: string): Promise<Client> {
    return this.apiClient.put<Client>(`/billing/clients/${clientId}`, command, token);
  }

  async deleteClient(clientId: number, token: string): Promise<void> {
    return this.apiClient.delete(`/billing/clients/${clientId}`, token);
  }

  async getInvoices(query: PageQuery, token: string): Promise<PageResult<Invoice>> {
    return this.apiClient.get<PageResult<Invoice>>(
      `/billing/invoices?page=${query.page}&size=${query.size}`,
      token
    );
  }

  async getInvoicesByClientId(clientId: number, query: PageQuery, token: string): Promise<PageResult<Invoice>> {
    return this.apiClient.get<PageResult<Invoice>>(
      `/billing/invoices/client/${clientId}?page=${query.page}&size=${query.size}`,
      token
    );
  }

  async getInvoiceById(invoiceId: number, token: string): Promise<Invoice> {
    return this.apiClient.get<Invoice>(`/billing/invoices/${invoiceId}`, token);
  }

  async createInvoice(command: SaveInvoiceCommand, token: string): Promise<Invoice> {
    return this.apiClient.post<Invoice>("/billing/invoices", command, token);
  }

  async updateInvoice(invoiceId: number, command: SaveInvoiceCommand, token: string): Promise<Invoice> {
    return this.apiClient.put<Invoice>(`/billing/invoices/${invoiceId}`, command, token);
  }

  async emitInvoice(invoiceId: number, token: string): Promise<Blob> {
    return this.apiClient.putForBlob(`/billing/invoices/${invoiceId}/emit`, token);
  }

  async downloadInvoicePdf(invoiceId: number, token: string): Promise<Blob> {
    return this.apiClient.getBlob(`/billing/invoices/${invoiceId}/pdf`, token);
  }

  async payInvoice(invoiceId: number, token: string): Promise<Invoice> {
    return this.apiClient.put<Invoice>(`/billing/invoices/${invoiceId}/pay`, {}, token);
  }

  async deleteInvoice(invoiceId: number, token: string): Promise<void> {
    return this.apiClient.delete(`/billing/invoices/${invoiceId}`, token);
  }
}
