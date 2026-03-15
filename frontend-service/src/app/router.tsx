import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppShell } from "@/ui/layout/AppShell";
import { ClientDetailPage } from "@/ui/pages/ClientDetailPage";
import { ClientFormPage } from "@/ui/pages/ClientFormPage";
import { ClientsWorkspacePage } from "@/ui/pages/ClientsWorkspacePage";
import { DashboardOverviewPage } from "@/ui/pages/DashboardOverviewPage";
import { InvoiceFormPage } from "@/ui/pages/InvoiceFormPage";
import { InvoicesWorkspacePage } from "@/ui/pages/InvoicesWorkspacePage";
import { LoginPage } from "@/ui/pages/LoginPage";
import { UsersPage } from "@/ui/pages/UsersPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <Navigate to="/login" replace />
  },
  {
    path: "/login",
    element: <LoginPage />
  },
  {
    path: "/",
    element: <AppShell />,
    children: [
      { path: "dashboard", element: <DashboardOverviewPage /> },
      { path: "clientes", element: <ClientsWorkspacePage /> },
      { path: "clientes/nuevo", element: <ClientFormPage /> },
      { path: "clientes/:clientId", element: <ClientDetailPage /> },
      { path: "clientes/:clientId/editar", element: <ClientFormPage /> },
      { path: "clientes/:clientId/facturas", element: <InvoicesWorkspacePage /> },
      { path: "facturas", element: <InvoicesWorkspacePage /> },
      { path: "facturas/nueva/:clientId", element: <InvoiceFormPage /> },
      { path: "facturas/:invoiceId/editar", element: <InvoiceFormPage /> },
      { path: "usuarios", element: <UsersPage /> }
    ]
  }
]);
