import { expect, test } from "@playwright/test";
import { login } from "./support/app";

test("should login against the real environment and open dashboard", async ({ page }, testInfo) => {
  await login(page);
  await expect(page.getByRole("heading", { name: /admin@mail.com/i })).toBeVisible();

  await page.screenshot({
    path: testInfo.outputPath("login-dashboard.png"),
    fullPage: true
  });
});
