import type { TokenStoragePort } from "@/application/ports/TokenStoragePort";
import type { AuthSession } from "@/domain/auth/AuthToken";

const STORAGE_KEY = "billmate.session";

export class LocalStorageTokenStorage implements TokenStoragePort {
  save(session: AuthSession): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  }

  clear(): void {
    localStorage.removeItem(STORAGE_KEY);
  }

  getToken(): string | null {
    return this.getSession()?.token ?? null;
  }

  getSession(): AuthSession | null {
    const session = localStorage.getItem(STORAGE_KEY);
    return session ? (JSON.parse(session) as AuthSession) : null;
  }
}
