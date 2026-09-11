"use client";

import { useSyncExternalStore } from "react";

/**
 * Hash router for the standalone build.
 *
 * The app is one page in the browser with no server, so the route lives in the
 * URL hash: `#/studio?module=palette`. Links stay real anchors, so middle-click
 * and copy-link behave, and `history.replaceState` is paired with an explicit
 * notify because it does not fire `hashchange`.
 */

type Listener = () => void;
const listeners = new Set<Listener>();

function notify() {
  for (const listener of listeners) listener();
}

export function subscribe(listener: Listener) {
  /*
   * One window listener serves every subscriber, so it is reference-counted
   * against the set rather than attached per subscriber. `notify` is a stable
   * module-level function, so repeat `addEventListener` calls collapse into a
   * single registration and the first `removeEventListener` would tear that
   * one registration down for everybody -- leaving the studio page used to
   * kill routing for the Shell that was still mounted, and every later
   * navigation changed the URL while the page stayed put.
   */
  if (listeners.size === 0) window.addEventListener("hashchange", notify);
  listeners.add(listener);
  return () => {
    if (!listeners.delete(listener)) return;
    if (listeners.size === 0) window.removeEventListener("hashchange", notify);
  };
}

function readHash(): string {
  if (typeof window === "undefined") return "/";
  const raw = window.location.hash.replace(/^#/, "");
  return raw.startsWith("/") ? raw : "/";
}

export function useRoute(): { path: string; search: string } {
  const hash = useSyncExternalStore(subscribe, readHash, () => "/");
  const [path, search = ""] = hash.split("?");
  return { path: path || "/", search };
}

export function navigate(to: string, replace = false) {
  const next = `#${to}`;
  if (replace) window.history.replaceState(null, "", next);
  else window.history.pushState(null, "", next);
  notify();
}

/** Accepts both `href="/studio"` and `href={{ pathname, query }}`. */
export function hrefToPath(href: unknown): string {
  if (typeof href === "string") return href;
  if (href && typeof href === "object") {
    const { pathname, query } = href as { pathname?: string; query?: Record<string, string> };
    const search = query ? new URLSearchParams(query).toString() : "";
    return `${pathname ?? "/"}${search ? `?${search}` : ""}`;
  }
  return "/";
}
