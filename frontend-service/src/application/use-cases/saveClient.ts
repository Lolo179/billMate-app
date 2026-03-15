import { z } from "zod";
import type { BillingApiPort, SaveClientCommand } from "@/application/ports/BillingApiPort";
import type { Client } from "@/domain/clients/Client";

const saveClientSchema = z.object({
  name: z.string().min(2).max(255),
  email: z.string().email(),
  phone: z.string().min(3).max(50),
  nif: z.string().min(3).max(50),
  address: z.string().min(3).max(255)
});

export async function saveClient(
  command: SaveClientCommand,
  token: string,
  billingApi: BillingApiPort,
  clientId?: number
): Promise<Client> {
  saveClientSchema.parse(command);

  if (clientId) {
    return billingApi.updateClient(clientId, command, token);
  }

  return billingApi.createClient(command, token);
}
