import type { BillingApiPort } from "@/application/ports/BillingApiPort";
import type { Client } from "@/domain/clients/Client";

export async function loadClient(
  clientId: number,
  token: string,
  billingApi: BillingApiPort
): Promise<Client> {
  return billingApi.getClientById(clientId, token);
}
