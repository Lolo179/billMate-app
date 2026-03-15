import type { BillingApiPort, PageQuery, PageResult } from "@/application/ports/BillingApiPort";
import type { Invoice } from "@/domain/invoices/Invoice";
import { DEFAULT_PAGE_SIZE } from "@/application/use-cases/loadClients";

const DEFAULT_QUERY: PageQuery = {
  page: 0,
  size: DEFAULT_PAGE_SIZE
};

export async function loadInvoicesPage(
  token: string,
  billingApi: BillingApiPort,
  query: PageQuery = DEFAULT_QUERY
): Promise<PageResult<Invoice>> {
  return billingApi.getInvoices(query, token);
}

export async function loadInvoices(token: string, billingApi: BillingApiPort): Promise<Invoice[]> {
  const firstPage = await loadInvoicesPage(token, billingApi);

  if (firstPage.totalPages <= 1) {
    return firstPage.items;
  }

  const remainingPages = await Promise.all(
    Array.from({ length: firstPage.totalPages - 1 }, (_, index) =>
      loadInvoicesPage(token, billingApi, { page: index + 1, size: firstPage.size })
    )
  );

  return [firstPage, ...remainingPages].flatMap((page) => page.items);
}
