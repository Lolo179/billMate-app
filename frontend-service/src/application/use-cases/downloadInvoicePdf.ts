import type { BillingApiPort } from "@/application/ports/BillingApiPort";

export async function downloadInvoicePdf(
  invoiceId: number,
  token: string,
  billingApi: BillingApiPort
): Promise<Blob> {
  return billingApi.downloadInvoicePdf(invoiceId, token);
}
