import type { BillingApiPort } from "@/application/ports/BillingApiPort";
import type { Invoice } from "@/domain/invoices/Invoice";

export async function loadInvoice(
  invoiceId: number,
  token: string,
  billingApi: BillingApiPort
): Promise<Invoice> {
  return billingApi.getInvoiceById(invoiceId, token);
}
