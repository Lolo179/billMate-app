import type {
  AuthApiPort,
  LoginCommand,
  RegisterUserCommand
} from "@/application/ports/AuthApiPort";
import type { AuthSession } from "@/domain/auth/AuthToken";
import type { User } from "@/domain/users/User";
import { ApiClient } from "@/infrastructure/http/ApiClient";

interface AuthResponse {
  token: string;
}

interface JwtPayload {
  sub: string;
  roles: Array<"ADMIN" | "USER">;
}

export class AuthHttpApiAdapter implements AuthApiPort {
  constructor(private readonly apiClient: ApiClient) {}

  async login(command: LoginCommand): Promise<AuthSession> {
    const response = await this.apiClient.post<AuthResponse>("/auth/login", command);
    const payload = parseJwtPayload(response.token);

    return {
      token: response.token,
      email: payload.sub,
      roles: payload.roles
    };
  }

  async getUsers(token: string): Promise<User[]> {
    return this.apiClient.get<User[]>("/auth/users", token);
  }

  async registerUser(command: RegisterUserCommand, token: string): Promise<void> {
    await this.apiClient.postWithoutResponse("/auth/register", command, token);
  }
}

function parseJwtPayload(token: string): JwtPayload {
  const [, payload] = token.split(".");

  if (!payload) {
    throw new Error("Token JWT inválido");
  }

  return JSON.parse(atob(payload)) as JwtPayload;
}
