"use client";

import {
  createContext, useCallback, useContext, useEffect, useMemo, useRef, useState,
  type ReactNode,
} from "react";

/**
 * Toasts.
 *
 * Approve and Refresh used to change state silently — you clicked, something
 * moved, and you inferred the rest. A toast says what happened in the words of
 * the action that caused it, then gets out of the way.
 *
 * Motion is deliberately cheap: transform and opacity only, one shared curve,
 * and an exit that runs on a timer rather than blocking anything. Reduced
 * motion is handled globally in the stylesheet, and the live region announces
 * the text either way.
 */

export type ToastTone = "neutral" | "go" | "signal";

interface Toast {
  id: number;
  text: string;
  detail?: string;
  tone: ToastTone;
  leaving: boolean;
}

interface ToastApi {
  show: (text: string, options?: { detail?: string; tone?: ToastTone }) => void;
}

const ToastContext = createContext<ToastApi | null>(null);

const VISIBLE_MS = 3600;
const EXIT_MS = 180;
/** More than three at once is noise, not feedback. */
const MAX = 3;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const nextId = useRef(0);
  const timers = useRef(new Map<number, ReturnType<typeof setTimeout>>());

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.map((t) => (t.id === id ? { ...t, leaving: true } : t)));
    const timer = setTimeout(() => {
      setToasts((current) => current.filter((t) => t.id !== id));
      timers.current.delete(id);
    }, EXIT_MS);
    timers.current.set(id, timer);
  }, []);

  const show = useCallback<ToastApi["show"]>(
    (text, options) => {
      const id = nextId.current;
      nextId.current += 1;
      setToasts((current) => [
        ...current.slice(-(MAX - 1)),
        { id, text, detail: options?.detail, tone: options?.tone ?? "neutral", leaving: false },
      ]);
      const timer = setTimeout(() => dismiss(id), VISIBLE_MS);
      timers.current.set(id, timer);
    },
    [dismiss],
  );

  useEffect(() => {
    const pending = timers.current;
    return () => {
      for (const timer of pending.values()) clearTimeout(timer);
      pending.clear();
    };
  }, []);

  const api = useMemo(() => ({ show }), [show]);

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div
        /*
         * Lifted clear of the studio's sticky action bar. A toast that covers
         * Approve is worse than no toast.
         */
        className="pointer-events-none fixed inset-x-0 bottom-0 z-50 flex flex-col items-center gap-2 p-4 sm:items-end sm:pb-24"
        role="status"
        aria-live="polite"
      >
        {toasts.map((toast) => (
          <button
            key={toast.id}
            type="button"
            onClick={() => dismiss(toast.id)}
            style={{
              transition: `opacity ${EXIT_MS}ms var(--ease-out-quint), transform ${EXIT_MS}ms var(--ease-out-quint)`,
              opacity: toast.leaving ? 0 : 1,
              transform: toast.leaving ? "translateY(6px)" : "none",
            }}
            className={`toast-in pointer-events-auto flex max-w-sm items-start gap-2.5 rounded-lg border px-3.5 py-2.5 text-left shadow-[0_8px_30px_-12px_rgba(23,21,15,0.35)] ${
              toast.tone === "go"
                ? "border-go/25 bg-go-soft"
                : toast.tone === "signal"
                  ? "border-signal/25 bg-signal-soft"
                  : "border-line bg-panel"
            }`}
          >
            <span
              aria-hidden
              className={`mt-0.5 text-body ${
                toast.tone === "go" ? "text-go" : toast.tone === "signal" ? "text-signal" : "text-ink-faint"
              }`}
            >
              {toast.tone === "go" ? "✓" : toast.tone === "signal" ? "!" : "↻"}
            </span>
            <span className="min-w-0">
              <span className="block text-body font-medium">{toast.text}</span>
              {toast.detail ? (
                <span className="mt-0.5 block text-micro leading-relaxed text-ink-soft">{toast.detail}</span>
              ) : null}
            </span>
          </button>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastApi {
  const api = useContext(ToastContext);
  // A missing provider should never break an action; it just means no toast.
  return api ?? { show: () => {} };
}
