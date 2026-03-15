import type { UserRole } from "@/domain/auth/AuthToken";

export interface User {
  id: number;
  username: string;
  email: string;
  roles: UserRole[];
}
