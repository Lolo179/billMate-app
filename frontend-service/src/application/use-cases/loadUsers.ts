import type { AuthApiPort } from "@/application/ports/AuthApiPort";
import type { User } from "@/domain/users/User";

export async function loadUsers(token: string, authApi: AuthApiPort): Promise<User[]> {
  return authApi.getUsers(token);
}
