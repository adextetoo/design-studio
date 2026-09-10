"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";

/* Small, unopinionated primitives. The studio chrome stays quiet so the
   client's brand is the only loud thing on any screen. */

export function Panel({
  children, className = "", flush = false,
}: { children: ReactNode; className?: string; flush?: boolean }) {
  return (
    <section className={`rounded-lg bg-panel ${flush ? "" : "p-5"} ${className}`}>
      {children}
    </section>
  );
}

export function Kicker({ children, className = "" }: { children: ReactNode; className?: string }) {
  return (
    <p className={`font-data text-micro uppercase tracking-[0.1em] text-ink-faint ${className}`}>
      {children}
    </p>
  );
}

export function SectionHead({
  title, note, action,
}: { title: string; note?: string; action?: ReactNode }) {
  return (
    <header className="mb-4 flex items-end justify-between gap-4">
      <div>
        <h2 className="text-subhead tracking-[-0.01em]">{title}</h2>
        {note ? <p className="mt-0.5 text-body text-ink-soft">{note}</p> : null}
      </div>
      {action}
    </header>
  );
}

type Tone = "neutral" | "go" | "signal" | "draft";

const TONES: Record<Tone, string> = {
  neutral: "bg-sunken text-ink-soft",
  go: "bg-go-soft text-go",
  signal: "bg-signal-soft text-signal",
  draft: "bg-sunken text-ink-faint",
};

export function Pill({
  children, tone = "neutral", dot = false,
}: { children: ReactNode; tone?: Tone; dot?: boolean }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2 py-0.5 text-micro font-medium ${TONES[tone]}`}
    >
      {dot ? <span className="size-1.5 rounded-full bg-current" aria-hidden /> : null}
      {children}
    </span>
  );
}

type Variant = "primary" | "secondary" | "ghost" | "danger";

const VARIANTS: Record<Variant, string> = {
  primary: "bg-ink text-panel hover:bg-ink/90 disabled:bg-ink/30",
  secondary: "border border-line bg-panel text-ink hover:bg-sunken disabled:text-ink-faint",
  ghost: "text-ink-soft hover:bg-sunken hover:text-ink",
  danger: "border border-signal/30 bg-signal-soft text-signal hover:bg-signal/10",
};

export function Button({
  children, variant = "secondary", className = "", ...rest
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }) {
  return (
    <button
      type="button"
      {...rest}
      className={`inline-flex items-center justify-center gap-2 rounded-md px-3 py-1.5 text-body font-medium transition-colors disabled:cursor-not-allowed ${VARIANTS[variant]} ${className}`}
    >
      {children}
    </button>
  );
}

export function StatTile({
  label, value, note,
}: { label: string; value: string; note?: string }) {
  return (
    <div className="border-b border-line-soft px-5 py-4 last:border-r-0 sm:border-b-0 sm:border-r [&:nth-last-child(-n+2)]:border-b-0 md:[&:nth-last-child(-n+2)]:border-b-0">
      <p className="font-data text-micro uppercase tracking-[0.08em] text-ink-faint">{label}</p>
      <p className="mt-1.5 font-display text-display tracking-[-0.02em] tabular-nums">{value}</p>
      {note ? <p className="mt-1 text-micro text-ink-faint">{note}</p> : null}
    </div>
  );
}

export function EmptyState({
  title, note, action,
}: { title: string; note: string; action?: ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 rounded-lg border border-dashed border-line px-6 py-12 text-center">
      <p className="text-body font-medium">{title}</p>
      <p className="max-w-sm text-body text-ink-soft">{note}</p>
      {action ? <div className="mt-2">{action}</div> : null}
    </div>
  );
}

export function Field({
  label, hint, children,
}: { label: string; hint?: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1 block text-body font-medium text-ink-soft">{label}</span>
      {children}
      {hint ? <span className="mt-1 block text-micro text-ink-faint">{hint}</span> : null}
    </label>
  );
}

export const inputClass =
  "w-full rounded-md border border-line bg-panel px-3 py-2 text-lead text-ink placeholder:text-ink-faint focus:border-ink focus:outline-none";

/** A hairline rule with a label, used to break long documents into passes. */
export function RuledHead({ children }: { children: ReactNode }) {
  return (
    <div className="flex items-center gap-3 py-1">
      <Kicker className="shrink-0">{children}</Kicker>
      <span className="h-px flex-1 bg-line" aria-hidden />
    </div>
  );
}

export function ProgressBar({ value, total }: { value: number; total: number }) {
  const pct = total === 0 ? 0 : Math.round((value / total) * 100);
  return (
    <div
      className="h-1 w-full overflow-hidden rounded-full bg-sunken"
      role="progressbar"
      aria-valuenow={value}
      aria-valuemin={0}
      aria-valuemax={total}
    >
      <div className="h-full rounded-full bg-ink transition-[width] duration-300" style={{ width: `${pct}%` }} />
    </div>
  );
}
