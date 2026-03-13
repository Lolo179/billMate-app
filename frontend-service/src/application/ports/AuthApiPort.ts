import type { AuthSession } from "@/domain/auth/AuthToken";
import type { User } from "@/domain/users/User";

export interface LoginCommand {
  email: string;
  password: string;
}

export interface RegisterUserCommand {
  username: string;
  email: string;
  password: string;
  role: "ADMIN" | "USER";
}

export interface AuthApiPort {
  login(command: LoginCommand): Promise<AuthSession>;
  getUsers(token: string): Promise<User[]>;
  registerUser(command: RegisterUserCommand, token: string): Promise<void>;
}
