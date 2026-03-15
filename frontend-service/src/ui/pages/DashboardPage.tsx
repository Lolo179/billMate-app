import { useEffect, useState } from "react";
import { loadDashboardSummary, type DashboardSummary } from "@/application/use-cases/loadDashboardSummary";
import { container } from "@/infrastructure/container";
import { InfoCard } from "@/ui/components/InfoCard";

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = container.tokenStorage.getToken();

    if (!token) {
      setError("No hay sesión activa");
      return;
    }

    loadDashboardSummary(token, container.billingApi)
      .then(setSummary)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <section className="page-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Resumen</p>
          <h3>Panel operativo</h3>
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <div className="card-grid">
        <InfoCard title="Clientes" value={String(summary?.clientsCount ?? "-")} />
        <InfoCard title="Facturas" value={String(summary?.invoicesCount ?? "-")} />
        <InfoCard
          title="Facturación"
          value={summary ? `${summary.totalInvoiced.toFixed(2)} €` : "-"}
          detail="Agregado desde billing-service a través del gateway"
        />
      </div>
    </section>
  );
}
