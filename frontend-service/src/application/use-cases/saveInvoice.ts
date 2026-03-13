import { z } from "zod";
import type { BillingApiPort, SaveInvoiceCommand } from "@/application/ports/BillingApiPort";
import type { Invoice } from "@/domain/invoices/Invoice";

const invoiceLineSchema = z.object({
  description: z.string().min(3).max(100),
  quantity: z.number().positive(),
  unitPrice: z.number().positive()
});

const saveInvoiceSchema = z.object({
  clientId: z.number().int().positive(),
  description: z.string().min(3).max(255),
  date: z.string().min(1),
  status: z.enum(["DRAFT", "SENT", "PAID", "CANCELLED"]).optional(),
  invoiceLines: z.array(invoiceLineSchema).min(1)
});

export async function saveInvoice(
  command: SaveInvoiceCommand,
  token: string,
  billingApi: BillingApiPort,
  invoiceId?: number
): Promise<Invoice> {
  saveInvoiceSchema.parse(command);

  if (invoiceId) {
    return billingApi.updateInvoice(invoiceId, command, token);
  }

  return billingApi.createInvoice(command, token);
}
