import type { BillingApiPort, PageQuery, PageResult } from "@/application/ports/BillingApiPort";
import type { Invoice } from "@/domain/invoices/Invoice";
import { DEFAULT_PAGE_SIZE } from "@/application/use-cases/loadClients";

const DEFAULT_QUERY: PageQuery = {
  page: 0,
  size: DEFAULT_PAGE_SIZE
};

export async function loadInvoicesByClientPage(
  clientId: number,
  token: string,
  billingApi: BillingApiPort,
  query: PageQuery = DEFAULT_QUERY
): Promise<PageResult<Invoice>> {
  return billingApi.getInvoicesByClientId(clientId, query, token);
}

export async function loadInvoicesByClient(
  clientId: number,
  token: string,
  billingApi: BillingApiPort
): Promise<Invoice[]> {
  const firstPage = await loadInvoicesByClientPage(clientId, token, billingApi);

  if (firstPage.totalPages <= 1) {
    return firstPage.items;
  }

  const remainingPages = await Promise.all(
    Array.from({ length: firstPage.totalPages - 1 }, (_, index) =>
      loadInvoicesByClientPage(clientId, token, billingApi, { page: index + 1, size: firstPage.size })
    )
  );

  return [firstPage, ...remainingPages].flatMap((page) => page.items);
}
