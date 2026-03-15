import type { AuthSession } from "@/domain/auth/AuthToken";

export interface TokenStoragePort {
  save(session: AuthSession): void;
  clear(): void;
  getToken(): string | null;
  getSession(): AuthSession | null;
}
