import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "@/application/use-cases/loginUser";
import { container } from "@/infrastructure/container";

export function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("admin@mail.com");
  const [password, setPassword] = useState("admin123");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await loginUser({ email, password }, container.authApi, container.tokenStorage);
      navigate("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error inesperado");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="login-layout">
      <section className="login-panel hero">
        <p className="eyebrow">BillMate</p>
        <h1>Frontend desacoplado para microservicios</h1>
        <p>
          Esta UI no conoce los servicios internos. Solo consume el gateway configurado por
          entorno o un backend simulado con MSW.
        </p>
      </section>

      <section className="login-panel form-panel">
        <form className="login-form" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </div>

          <div>
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>

          {error ? <p className="error-banner">{error}</p> : null}

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Iniciando..." : "Iniciar sesión"}
          </button>
        </form>
      </section>
    </div>
  );
}
