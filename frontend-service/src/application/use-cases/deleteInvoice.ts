import type { BillingApiPort } from "@/application/ports/BillingApiPort";

export async function deleteInvoice(
  invoiceId: number,
  token: string,
  billingApi: BillingApiPort
): Promise<void> {
  return billingApi.deleteInvoice(invoiceId, token);
}
