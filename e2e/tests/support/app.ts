import { expect } from "@playwright/test";
import type { Page } from "@playwright/test";

const loginEmail = process.env.E2E_LOGIN_EMAIL ?? "admin@mail.com";
const loginPassword = process.env.E2E_LOGIN_PASSWORD ?? "admin123";
const candidateBaseUrls = [
  process.env.E2E_BASE_URL,
  "http://localhost:5173",
  "http://127.0.0.1:5173"
].filter((value): value is string => Boolean(value));

export async function login(page: Page): Promise<void> {
  const baseUrl = await resolveReachableBaseUrl();

  await page.goto(`${baseUrl}/login`);
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole("heading", { name: /Frontend desacoplado para microservicios/i })).toBeVisible();

  await page.locator("#email").fill(loginEmail);
  await page.locator("#password").fill(loginPassword);
  await page.getByRole("button", { name: /Iniciar sesi/i }).click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole("heading", { name: /Panel operativo/i })).toBeVisible();
}

export async function resolveReachableBaseUrl(): Promise<string> {
  for (const baseUrl of candidateBaseUrls) {
    try {
      const response = await fetch(`${baseUrl}/login`, {
        method: "GET",
        redirect: "manual"
      });

      if (response.ok || isRedirect(response.status)) {
        return baseUrl;
      }
    } catch {
      // Sigue probando otras URLs permitidas para el frontend React.
    }
  }

  throw new Error(
    `No se encontro ningun frontend accesible. URLs comprobadas: ${candidateBaseUrls.join(", ")}. ` +
      "Configura E2E_BASE_URL o levanta el frontend antes de ejecutar Playwright."
  );
}

function isRedirect(status: number): boolean {
  return status >= 300 && status < 400;
}
