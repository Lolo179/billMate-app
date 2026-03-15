import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { LoginPage } from "@/ui/pages/LoginPage";

vi.mock("@/infrastructure/container", () => ({
  container: {
    authApi: {
      login: vi.fn().mockResolvedValue({
        token: "token",
        email: "admin@mail.com",
        roles: ["ADMIN"]
      })
    },
    tokenStorage: {
      save: vi.fn(),
      clear: vi.fn(),
      getToken: vi.fn(),
      getSession: vi.fn()
    }
  }
}));

describe("LoginPage", () => {
  it("shouldRenderLoginAction", async () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    await userEvent.click(screen.getByRole("button", { name: "Iniciar sesión" }));

    expect(screen.getByDisplayValue("admin@mail.com")).toBeInTheDocument();
  });
});
