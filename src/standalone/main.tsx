"use client";

import { createRoot } from "react-dom/client";
import { StrictMode } from "react";
import { Shell } from "@/components/Shell";
import { StoreProvider } from "@/lib/store";
import { ToastProvider } from "@/components/Toast";
import { useRoute } from "./router";

import OverviewPage from "@/app/page";
import DiscoveryPage from "@/app/discovery/page";
import StudioPage from "@/app/studio/page";
import HubPage from "@/app/hub/page";
import HandoffPage from "@/app/handoff/page";

import "@/app/globals.css";

const ROUTES: Record<string, () => React.ReactElement> = {
  "/": OverviewPage,
  "/discovery": DiscoveryPage,
  "/studio": StudioPage,
  "/hub": HubPage,
  "/handoff": HandoffPage,
};

function Router() {
  const { path } = useRoute();
  const Page = ROUTES[path] ?? OverviewPage;
  return <Page />;
}

function App() {
  return (
    <StoreProvider>
      <ToastProvider>
        <Shell>
          <Router />
        </Shell>
      </ToastProvider>
    </StoreProvider>
  );
}

const mount = document.getElementById("root");
if (mount) {
  createRoot(mount).render(
    <StrictMode>
      <App />
    </StrictMode>,
  );
}
