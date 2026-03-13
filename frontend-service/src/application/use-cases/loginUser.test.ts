import { describe, expect, it, vi } from "vitest";
import type { AuthApiPort } from "@/application/ports/AuthApiPort";
import type { TokenStoragePort } from "@/application/ports/TokenStoragePort";
import { loginUser } from "@/application/use-cases/loginUser";

describe("loginUser", () => {
  it("shouldPersistSessionWhenCredentialsAreValid", async () => {
    const session = {
      token: "token",
      email: "admin@mail.com",
      roles: ["ADMIN"] as const
    };

    const authApi: AuthApiPort = {
      login: vi.fn().mockResolvedValue(session),
      getUsers: vi.fn(),
      registerUser: vi.fn()
    };

    const tokenStorage: TokenStoragePort = {
      save: vi.fn(),
      clear: vi.fn(),
      getToken: vi.fn(),
      getSession: vi.fn()
    };

    const result = await loginUser(
      { email: "admin@mail.com", password: "admin123" },
      authApi,
      tokenStorage
    );

    expect(result).toEqual(session);
    expect(tokenStorage.save).toHaveBeenCalledWith(session);
  });
});
