import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { loadClient } from "@/application/use-cases/loadClient";
import { saveClient } from "@/application/use-cases/saveClient";
import type { SaveClientCommand } from "@/application/ports/BillingApiPort";
import { container } from "@/infrastructure/container";

const emptyClient: SaveClientCommand = {
  name: "",
  email: "",
  phone: "",
  nif: "",
  address: ""
};

export function ClientFormPage() {
  const navigate = useNavigate();
  const params = useParams<{ clientId: string }>();
  const clientId = params.clientId ? Number(params.clientId) : undefined;
  const token = container.tokenStorage.getToken();

  const [form, setForm] = useState<SaveClientCommand>(emptyClient);
  const [loading, setLoading] = useState(Boolean(clientId));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!clientId || !token) {
      setLoading(false);
      return;
    }

    loadClient(clientId, token, container.billingApi)
      .then((client) => {
        setForm({
          name: client.name,
          email: client.email,
          phone: client.phone,
          nif: client.nif,
          address: client.address
        });
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  }, [clientId, token]);

  function handleChange(field: keyof SaveClientCommand, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    try {
      setSaving(true);
      setError(null);
      const client = await saveClient(form, token, container.billingApi, clientId);
      navigate(`/clientes/${client.clientId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo guardar el cliente");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Billing</p>
          <h3>{clientId ? "Editar cliente" : "Nuevo cliente"}</h3>
          <p className="section-copy">Completa los datos basicos para reutilizar el cliente en nuevas facturas.</p>
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      {loading ? (
        <p className="muted-copy">Cargando cliente...</p>
      ) : (
        <form className="stack-form" onSubmit={handleSubmit} aria-busy={saving}>
          <label>
            Nombre
            <input value={form.name} onChange={(event) => handleChange("name", event.target.value)} />
          </label>
          <label>
            Email
            <input
              type="email"
              value={form.email}
              onChange={(event) => handleChange("email", event.target.value)}
            />
          </label>
          <label>
            Teléfono
            <input value={form.phone} onChange={(event) => handleChange("phone", event.target.value)} />
          </label>
          <label>
            NIF
            <input value={form.nif} onChange={(event) => handleChange("nif", event.target.value)} />
          </label>
          <label>
            Dirección
            <input
              value={form.address}
              onChange={(event) => handleChange("address", event.target.value)}
            />
          </label>
          <div className="button-row">
            <button className="primary-button inline-button" type="submit" disabled={saving}>
              {saving ? "Guardando..." : clientId ? "Guardar cambios" : "Crear cliente"}
            </button>
            <button
              className="secondary-button inline-button"
              type="button"
              onClick={() => navigate(clientId ? `/clientes/${clientId}` : "/clientes")}
              disabled={saving}
            >
              Cancelar
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
