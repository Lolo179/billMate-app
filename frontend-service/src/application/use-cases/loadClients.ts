import type { BillingApiPort, PageQuery, PageResult } from "@/application/ports/BillingApiPort";
import type { Client } from "@/domain/clients/Client";

export const DEFAULT_PAGE_SIZE = 20;

const DEFAULT_QUERY: PageQuery = {
  page: 0,
  size: DEFAULT_PAGE_SIZE
};

export async function loadClientsPage(
  token: string,
  billingApi: BillingApiPort,
  query: PageQuery = DEFAULT_QUERY
): Promise<PageResult<Client>> {
  return billingApi.getClients(query, token);
}

export async function loadClients(token: string, billingApi: BillingApiPort): Promise<Client[]> {
  const firstPage = await loadClientsPage(token, billingApi);

  if (firstPage.totalPages <= 1) {
    return firstPage.items;
  }

  const remainingPages = await Promise.all(
    Array.from({ length: firstPage.totalPages - 1 }, (_, index) =>
      loadClientsPage(token, billingApi, { page: index + 1, size: firstPage.size })
    )
  );

  return [firstPage, ...remainingPages].flatMap((page) => page.items);
}
