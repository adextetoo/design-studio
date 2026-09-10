"use client";

import { navigate, useRoute } from "./router";

/** Stands in for `next/navigation` in the standalone build. */

export function usePathname(): string {
  return useRoute().path;
}

export function useSearchParams(): URLSearchParams {
  return new URLSearchParams(useRoute().search);
}

export function useRouter() {
  return {
    push: (to: string) => navigate(to, false),
    replace: (to: string) => navigate(to, true),
    back: () => window.history.back(),
    forward: () => window.history.forward(),
    refresh: () => {},
    prefetch: () => {},
  };
}
