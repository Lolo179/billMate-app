import type { BillingApiPort } from "@/application/ports/BillingApiPort";
import { loadClientsPage } from "@/application/use-cases/loadClients";
import { loadInvoicesPage } from "@/application/use-cases/loadInvoices";

export interface DashboardSummary {
  clientsCount: number;
  invoicesCount: number;
  totalInvoiced: number;
}

export async function loadDashboardSummary(
  token: string,
  billingApi: BillingApiPort
): Promise<DashboardSummary> {
  const [clientsPage, invoicesPage] = await Promise.all([
    loadClientsPage(token, billingApi),
    loadInvoicesPage(token, billingApi)
  ]);

  const remainingInvoices =
    invoicesPage.totalPages > 1
      ? await Promise.all(
          Array.from({ length: invoicesPage.totalPages - 1 }, (_, index) =>
            loadInvoicesPage(token, billingApi, { page: index + 1, size: invoicesPage.size })
          )
        )
      : [];

  const allInvoices = [invoicesPage, ...remainingInvoices].flatMap((page) => page.items);

  return {
    clientsCount: clientsPage.totalElements,
    invoicesCount: invoicesPage.totalElements,
    totalInvoiced: allInvoices.reduce((sum, invoice) => sum + invoice.total, 0)
  };
}
