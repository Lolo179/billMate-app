import type { BillingApiPort } from "@/application/ports/BillingApiPort";

export async function deleteClient(
  clientId: number,
  token: string,
  billingApi: BillingApiPort
): Promise<void> {
  return billingApi.deleteClient(clientId, token);
}
