export type UserRole = "ADMIN" | "USER";

export interface AuthSession {
  token: string;
  email: string;
  roles: UserRole[];
}
