import { http, HttpResponse } from "msw";
import type { Client } from "@/domain/clients/Client";
import type { Invoice } from "@/domain/invoices/Invoice";
import type { User } from "@/domain/users/User";
import type { PageResult } from "@/application/ports/BillingApiPort";

const mockToken =
  "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBtYWlsLmNvbSIsInJvbGVzIjpbIkFETUlOIl19.signature";

const mockUsers: User[] = [
  { id: 1, username: "admin", email: "admin@mail.com", roles: ["ADMIN"] },
  { id: 2, username: "demo", email: "demo@mail.com", roles: ["USER"] }
];

let clients: Client[] = [
  {
    clientId: 1,
    name: "Acme Corp",
    email: "billing@acme.test",
    phone: "600123123",
    nif: "B12345678",
    address: "Calle Mayor 1",
    createdAt: "2026-03-01T10:00:00Z"
  },
  {
    clientId: 2,
    name: "Globex",
    email: "finance@globex.test",
    phone: "611222333",
    nif: "B87654321",
    address: "Avenida Europa 22",
    createdAt: "2026-03-02T10:00:00Z"
  }
];

let invoices: Invoice[] = [
  {
    invoiceId: 100,
    clientId: 1,
    clientName: "Acme Corp",
    description: "Factura mensual",
    date: "2026-03-01",
    invoiceLines: [
      { description: "Mantenimiento", quantity: 2, unitPrice: 500, total: 1000 },
      { description: "Soporte", quantity: 1, unitPrice: 250, total: 250 }
    ],
    total: 1250,
    status: "SENT",
    createdAt: "2026-03-01T10:00:00Z",
    taxPercentage: 21
  },
  {
    invoiceId: 101,
    clientId: 2,
    clientName: "Globex",
    description: "Servicios de consultoria",
    date: "2026-03-04",
    invoiceLines: [{ description: "Consultoria", quantity: 4, unitPrice: 200, total: 800 }],
    total: 800,
    status: "PAID",
    createdAt: "2026-03-04T09:00:00Z",
    taxPercentage: 21
  }
];

function paginate<T>(items: T[], requestUrl: URL): PageResult<T> {
  const page = Number(requestUrl.searchParams.get("page") ?? "0");
  const requestedSize = Number(requestUrl.searchParams.get("size") ?? "20");
  const size = Math.min(Math.max(requestedSize, 1), 20);
  const start = Math.max(page, 0) * size;
  const pagedItems = items.slice(start, start + size);

  return {
    items: pagedItems,
    page: Math.max(page, 0),
    size,
    totalElements: items.length,
    totalPages: Math.max(1, Math.ceil(items.length / size))
  };
}

export const handlersReact = [
  http.post("*/auth/login", async () => HttpResponse.json({ token: mockToken })),
  http.get("*/auth/users", async () => HttpResponse.json(mockUsers)),
  http.post("*/auth/register", async () => new HttpResponse(null, { status: 204 })),
  http.get("*/billing/clients", async ({ request }) => HttpResponse.json(paginate(clients, new URL(request.url)))),
  http.get("*/billing/clients/:clientId", async ({ params }) => {
    const client = clients.find((item) => item.clientId === Number(params.clientId));
    return client ? HttpResponse.json(client) : HttpResponse.json({ message: "Not found" }, { status: 404 });
  }),
  http.post("*/billing/clients", async ({ request }) => {
    const payload = (await request.json()) as Omit<Client, "clientId" | "createdAt">;
    const created: Client = {
      ...payload,
      clientId: Math.max(...clients.map((item) => item.clientId)) + 1,
      createdAt: new Date().toISOString()
    };
    clients = [...clients, created];
    return HttpResponse.json(created, { status: 201 });
  }),
  http.put("*/billing/clients/:clientId", async ({ params, request }) => {
    const payload = (await request.json()) as Partial<Client>;
    const clientId = Number(params.clientId);
    const existing = clients.find((item) => item.clientId === clientId);

    if (!existing) {
      return HttpResponse.json({ message: "Not found" }, { status: 404 });
    }

    const updated = { ...existing, ...payload };
    clients = clients.map((item) => (item.clientId === clientId ? updated : item));
    return HttpResponse.json(updated);
  }),
  http.delete("*/billing/clients/:clientId", async ({ params }) => {
    const clientId = Number(params.clientId);
    clients = clients.filter((item) => item.clientId !== clientId);
    invoices = invoices.filter((item) => item.clientId !== clientId);
    return new HttpResponse(null, { status: 204 });
  }),
  http.get("*/billing/invoices", async ({ request }) => HttpResponse.json(paginate(invoices, new URL(request.url)))),
  http.get("*/billing/invoices/client/:clientId", async ({ params, request }) =>
    HttpResponse.json(paginate(invoices.filter((item) => item.clientId === Number(params.clientId)), new URL(request.url)))
  ),
  http.get("*/billing/invoices/:invoiceId", async ({ params }) => {
    const invoice = invoices.find((item) => item.invoiceId === Number(params.invoiceId));
    return invoice ? HttpResponse.json(invoice) : HttpResponse.json({ message: "Not found" }, { status: 404 });
  }),
  http.post("*/billing/invoices", async ({ request }) => {
    const payload = (await request.json()) as Omit<Invoice, "invoiceId" | "total" | "clientName">;
    const created: Invoice = {
      ...payload,
      invoiceId: Math.max(...invoices.map((item) => item.invoiceId)) + 1,
      total: (payload.invoiceLines ?? []).reduce((sum, line) => sum + line.quantity * line.unitPrice, 0),
      clientName: clients.find((item) => item.clientId === payload.clientId)?.name,
      createdAt: new Date().toISOString(),
      taxPercentage: 21
    };
    invoices = [...invoices, created];
    return HttpResponse.json(created, { status: 201 });
  }),
  http.put("*/billing/invoices/:invoiceId", async ({ params, request }) => {
    const invoiceId = Number(params.invoiceId);
    const payload = (await request.json()) as Partial<Invoice>;
    const existing = invoices.find((item) => item.invoiceId === invoiceId);

    if (!existing) {
      return HttpResponse.json({ message: "Not found" }, { status: 404 });
    }

    const updated: Invoice = {
      ...existing,
      ...payload,
      total:
        payload.invoiceLines?.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0) ?? existing.total
    };
    invoices = invoices.map((item) => (item.invoiceId === invoiceId ? updated : item));
    return HttpResponse.json(updated);
  }),
  http.put("*/billing/invoices/:invoiceId/emit", async ({ params }) => {
    const invoiceId = Number(params.invoiceId);
    invoices = invoices.map((item) => (item.invoiceId === invoiceId ? { ...item, status: "SENT" } : item));
    return new HttpResponse(new Blob(["pdf"], { type: "application/pdf" }), {
      status: 200,
      headers: { "Content-Type": "application/pdf" }
    });
  }),
  http.put("*/billing/invoices/:invoiceId/pay", async ({ params }) => {
    const invoiceId = Number(params.invoiceId);
    const existing = invoices.find((item) => item.invoiceId === invoiceId);

    if (!existing) {
      return HttpResponse.json({ message: "Not found" }, { status: 404 });
    }

    const updated: Invoice = { ...existing, status: "PAID" };
    invoices = invoices.map((item) => (item.invoiceId === invoiceId ? updated : item));
    return HttpResponse.json(updated);
  }),
  http.get("*/billing/invoices/:invoiceId/pdf", async () =>
    new HttpResponse(new Blob(["pdf"], { type: "application/pdf" }), {
      status: 200,
      headers: { "Content-Type": "application/pdf" }
    })
  ),
  http.delete("*/billing/invoices/:invoiceId", async ({ params }) => {
    const invoiceId = Number(params.invoiceId);
    invoices = invoices.filter((item) => item.invoiceId !== invoiceId);
    return new HttpResponse(null, { status: 204 });
  })
];
