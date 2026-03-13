import type { Client } from "@/domain/clients/Client";
import type { Invoice } from "@/domain/invoices/Invoice";
import type { User } from "@/domain/users/User";

export const mockToken =
  "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBtYWlsLmNvbSIsInJvbGVzIjpbIkFETUlOIl19.signature";

export const mockClients: Client[] = [
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

export const mockInvoices: Invoice[] = [
  {
    invoiceId: 100,
    clientId: 1,
    clientName: "Acme Corp",
    description: "Factura mensual",
    date: "2026-03-01",
    total: 1250,
    status: "SENT"
  },
  {
    invoiceId: 101,
    clientId: 2,
    clientName: "Globex",
    description: "Servicios de consultoría",
    date: "2026-03-04",
    total: 800,
    status: "PAID"
  }
];

export const mockUsers: User[] = [
  {
    id: 1,
    username: "admin",
    email: "admin@mail.com",
    roles: ["ADMIN"]
  },
  {
    id: 2,
    username: "demo",
    email: "demo@mail.com",
    roles: ["USER"]
  }
];
