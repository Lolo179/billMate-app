import type { ReactNode } from "react";

interface InfoCardProps {
  title: string;
  value: string;
  detail?: ReactNode;
}

export function InfoCard({ title, value, detail }: InfoCardProps) {
  return (
    <article className="info-card">
      <p className="eyebrow">{title}</p>
      <strong>{value}</strong>
      {detail ? <span>{detail}</span> : null}
    </article>
  );
}
