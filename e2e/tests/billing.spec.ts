import { expect, test } from "@playwright/test";
import type { Page, TestInfo } from "@playwright/test";
import { login } from "./support/app";

test("should create a client", async ({ page }, testInfo) => {
  await login(page);

  const clientData = buildClientData(testInfo);

  await page.goto("/clientes/nuevo");
  await expect(page.getByRole("heading", { name: /Nuevo cliente/i })).toBeVisible();

  await page.getByLabel("Nombre").fill(clientData.name);
  await page.getByLabel("Email").fill(clientData.email);
  await page.getByLabel(/Tel/i).fill(clientData.phone);
  await page.getByLabel("NIF").fill(clientData.nif);
  await page.getByLabel(/Direcci/i).fill(clientData.address);
  await page.getByRole("button", { name: /Crear cliente/i }).click();

  await expect(page).toHaveURL(/\/clientes\/\d+$/);
  await expect(page.getByRole("heading", { name: clientData.name })).toBeVisible();
  await expect(page.getByText(clientData.email)).toBeVisible();
});

test("should create an invoice", async ({ page }, testInfo) => {
  await login(page);

  const clientData = buildClientData(testInfo);
  await createClient(page, clientData);

  await page.getByRole("link", { name: /Nueva factura/i }).click();
  const invoiceData = buildInvoiceData(testInfo);
  await fillInvoiceForm(page, invoiceData);

  await page.getByRole("button", { name: /Crear factura/i }).click();

  await expect(page).toHaveURL(/\/facturas\/\d+\/editar$/);
  await expect(page.getByRole("status")).toContainText(/Factura creada correctamente/i);
  await expect(page.getByRole("button", { name: /Emitir/i })).toBeVisible();
});

test("should emit an invoice", async ({ page }, testInfo) => {
  await login(page);

  const clientData = buildClientData(testInfo);
  await createClient(page, clientData);

  await page.getByRole("link", { name: /Nueva factura/i }).click();
  const invoiceData = buildInvoiceData(testInfo);
  await fillInvoiceForm(page, invoiceData);
  await page.getByRole("button", { name: /Crear factura/i }).click();

  const downloadPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: /^Emitir$/i }).click();
  const download = await downloadPromise;

  await expect(page.getByRole("status")).toContainText(/Factura emitida y PDF generado/i);
  await expect(page.getByRole("button", { name: /^Emitir$/i })).toHaveCount(0);
  await expect(page.getByRole("button", { name: /Descargar PDF/i })).toBeVisible();
  await expect(download.suggestedFilename()).toMatch(/factura_\d+\.pdf/i);
});

test("should download an invoice pdf", async ({ page }, testInfo) => {
  await login(page);

  const clientData = buildClientData(testInfo);
  await createClient(page, clientData);

  await page.getByRole("link", { name: /Nueva factura/i }).click();
  const invoiceData = buildInvoiceData(testInfo);
  await fillInvoiceForm(page, invoiceData);
  await page.getByRole("button", { name: /Crear factura/i }).click();

  const emitDownloadPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: /^Emitir$/i }).click();
  await emitDownloadPromise;

  const pdfDownloadPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: /Descargar PDF/i }).click();
  const pdfDownload = await pdfDownloadPromise;

  await expect(page.getByRole("status")).toContainText(/PDF descargado correctamente/i);
  await expect(pdfDownload.suggestedFilename()).toMatch(/factura_\d+\.pdf/i);
});

async function createClient(page: Page, clientData: ClientData): Promise<void> {
  await page.goto("/clientes/nuevo");
  await expect(page.getByRole("heading", { name: /Nuevo cliente/i })).toBeVisible();

  await page.getByLabel("Nombre").fill(clientData.name);
  await page.getByLabel("Email").fill(clientData.email);
  await page.getByLabel(/Tel/i).fill(clientData.phone);
  await page.getByLabel("NIF").fill(clientData.nif);
  await page.getByLabel(/Direcci/i).fill(clientData.address);
  await page.getByRole("button", { name: /Crear cliente/i }).click();

  await expect(page).toHaveURL(/\/clientes\/\d+$/);
  await expect(page.getByRole("heading", { name: clientData.name })).toBeVisible();
}

async function fillInvoiceForm(page: Page, invoiceData: InvoiceData): Promise<void> {
  await expect(page.getByRole("heading", { name: /Nueva factura/i })).toBeVisible();

  await page.getByLabel(/Descripci/i).fill(invoiceData.description);
  await fillInvoiceLine(page, 0, invoiceData.lineDescription, invoiceData.quantity, invoiceData.unitPrice);
}

async function fillInvoiceLine(
  page: Page,
  rowIndex: number,
  description: string,
  quantity: string,
  unitPrice: string
): Promise<void> {
  const row = page.locator("tbody tr").nth(rowIndex);
  const inputs = row.locator("input");

  await inputs.nth(0).fill(description);
  await inputs.nth(1).fill(quantity);
  await inputs.nth(2).fill(unitPrice);
}

function buildClientData(testInfo: TestInfo): ClientData {
  const suffix = buildSuffix(testInfo);
  const nif = buildValidTestNif(testInfo);

  return {
    name: `Cliente E2E ${suffix}`,
    email: `cliente.e2e.${suffix}@billmate.test`,
    phone: "600123123",
    nif,
    address: `Calle E2E ${suffix}`
  };
}

function buildInvoiceData(testInfo: TestInfo): InvoiceData {
  const suffix = buildSuffix(testInfo);

  return {
    description: `Factura E2E ${suffix}`,
    lineDescription: `Linea E2E ${suffix}`,
    quantity: "2",
    unitPrice: "125.50"
  };
}

function buildSuffix(testInfo: TestInfo): string {
  return `${Date.now()}-${testInfo.parallelIndex}`;
}

function buildValidTestNif(testInfo: TestInfo): string {
  const number = String((Date.now() + testInfo.parallelIndex) % 100_000_000).padStart(8, "0");
  const letters = "TRWAGMYFPDXBNJZSQVHLCKE";
  const letter = letters[Number(number) % 23];

  return `${number}${letter}`;
}

interface ClientData {
  name: string;
  email: string;
  phone: string;
  nif: string;
  address: string;
}

interface InvoiceData {
  description: string;
  lineDescription: string;
  quantity: string;
  unitPrice: string;
}
