import { env } from "@/infrastructure/config/env";

export async function initializeMocks(): Promise<void> {
  if (!env.useMsw) {
    return;
  }

  const { worker } = await import("@/mocks/browser");
  await worker.start({ onUnhandledRequest: "bypass" });
}
