import type { BillingApiPort } from "@/application/ports/BillingApiPort";

export async function emitInvoice(
  invoiceId: number,
  token: string,
  billingApi: BillingApiPort
): Promise<Blob> {
  return billingApi.emitInvoice(invoiceId, token);
}
