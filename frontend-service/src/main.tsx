import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { router } from "@/app/router";
import "@/ui/styles/global.css";
import { initializeMocks } from "@/mocks/initializeMocks";

async function bootstrap() {
  await initializeMocks();

  ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
      <RouterProvider router={router} />
    </React.StrictMode>
  );
}

void bootstrap();
