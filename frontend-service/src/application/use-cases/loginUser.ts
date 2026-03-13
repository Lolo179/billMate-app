import { z } from "zod";
import type { AuthApiPort, LoginCommand } from "@/application/ports/AuthApiPort";
import type { TokenStoragePort } from "@/application/ports/TokenStoragePort";
import type { AuthSession } from "@/domain/auth/AuthToken";

const loginSchema = z.object({
  email: z.string().email("Email inválido"),
  password: z.string().min(1, "La contraseña es obligatoria")
});

export async function loginUser(
  command: LoginCommand,
  authApi: AuthApiPort,
  tokenStorage: TokenStoragePort
): Promise<AuthSession> {
  loginSchema.parse(command);

  const session = await authApi.login(command);
  tokenStorage.save(session);

  return session;
}
