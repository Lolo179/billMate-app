import { http, HttpResponse } from "msw";
import { mockClients, mockInvoices, mockToken, mockUsers } from "@/mocks/data";

export const handlers = [
  http.post("*/auth/login", async () => HttpResponse.json({ token: mockToken })),
  http.get("*/auth/users", async () => HttpResponse.json(mockUsers)),
  http.post("*/auth/register", async () => new HttpResponse(null, { status: 204 })),
  http.get("*/billing/clients", async () => HttpResponse.json(mockClients)),
  http.get("*/billing/invoices", async () => HttpResponse.json(mockInvoices))
];
