import { AuthHttpApiAdapter } from "@/infrastructure/adapters/AuthHttpApiAdapter";
import { BillingHttpApiAdapter } from "@/infrastructure/adapters/BillingHttpApiAdapter";
import { env } from "@/infrastructure/config/env";
import { ApiClient } from "@/infrastructure/http/ApiClient";
import { LocalStorageTokenStorage } from "@/infrastructure/storage/LocalStorageTokenStorage";

const apiClient = new ApiClient(env.apiBaseUrl);

export const container = {
  authApi: new AuthHttpApiAdapter(apiClient),
  billingApi: new BillingHttpApiAdapter(apiClient),
  tokenStorage: new LocalStorageTokenStorage()
};
