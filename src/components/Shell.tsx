"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { MODULE_IDS } from "@/data/modules";
import { STEPS } from "@/data/workshop";
import { useStudio } from "@/lib/store";
import { Pill } from "./ui";

const NAV = [
  { href: "/", label: "Overview", glyph: "▦" },
  { href: "/discovery", label: "Discovery & Strategy", glyph: "◷" },
  { href: "/studio", label: "Design Studio", glyph: "◈" },
  { href: "/hub", label: "Brand Hub", glyph: "◫" },
  { href: "/handoff", label: "Handoff", glyph: "↓" },
] as const;

export function Shell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const { project, approvedCount } = useStudio();
  const answered = Object.values(project.workshop.answers).filter((v) => v.trim()).length;

  return (
    <div className="flex min-h-screen">
      <aside className="sticky top-0 hidden h-screen w-60 shrink-0 flex-col border-r border-line bg-panel md:flex">
        <div className="flex items-center gap-2.5 px-4 py-4">
          <span
            className="grid size-7 shrink-0 place-items-center rounded-md bg-ink text-[13px] font-bold text-panel"
            aria-hidden
          >
            ◆
          </span>
          <div className="min-w-0">
            <p className="truncate text-[13px] font-semibold leading-tight">Design Studio</p>
            <p className="truncate text-[11px] text-ink-faint">Brand systems, end to end</p>
          </div>
        </div>

        <nav className="px-2" aria-label="Studio">
          {NAV.map((item) => {
            const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                aria-current={active ? "page" : undefined}
                className={`mb-0.5 flex items-center gap-2.5 rounded-md px-2.5 py-1.5 text-[13px] transition-colors ${
                  active ? "bg-sunken font-medium text-ink" : "text-ink-soft hover:bg-sunken/60 hover:text-ink"
                }`}
              >
                <span className="w-3.5 text-center text-[11px] text-ink-faint" aria-hidden>{item.glyph}</span>
                <span className="truncate">{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="mt-6 px-4">
          <p className="mb-2 text-[11px] font-medium uppercase tracking-[0.12em] text-ink-faint">Client</p>
          <div className="flex items-center gap-2.5 rounded-md border border-line px-2.5 py-2">
            <span
              className="grid size-6 shrink-0 place-items-center rounded bg-ink text-[11px] font-bold text-panel"
              aria-hidden
            >
              {(project.brief.brandName || "?").charAt(0).toUpperCase()}
            </span>
            <div className="min-w-0">
              <p className="truncate text-[13px] font-medium leading-tight">
                {project.brief.brandName || "Untitled"}
              </p>
              <p className="truncate text-[11px] text-ink-faint">{project.brief.sector || "No sector set"}</p>
            </div>
          </div>
        </div>

        <div className="mt-auto space-y-2 px-4 py-4 text-[11px] text-ink-faint">
          <div className="flex items-center justify-between">
            <span>Discovery</span>
            <span className="tabular-nums">{answered} / {STEPS.length}</span>
          </div>
          <div className="flex items-center justify-between">
            <span>Modules approved</span>
            <span className="tabular-nums">{approvedCount} / {MODULE_IDS.length}</span>
          </div>
          <p className="pt-2 leading-relaxed">
            Saved in this browser. Nothing leaves the device.
          </p>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <MobileNav pathname={pathname} />
        <main className="min-w-0 flex-1">{children}</main>
      </div>
    </div>
  );
}

function MobileNav({ pathname }: { pathname: string }) {
  return (
    <nav
      className="sticky top-0 z-30 flex gap-1 overflow-x-auto border-b border-line bg-panel px-3 py-2 md:hidden"
      aria-label="Studio"
    >
      {NAV.map((item) => {
        const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
        return (
          <Link
            key={item.href}
            href={item.href}
            aria-current={active ? "page" : undefined}
            className={`whitespace-nowrap rounded-md px-2.5 py-1.5 text-[13px] ${
              active ? "bg-sunken font-medium" : "text-ink-soft"
            }`}
          >
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}

export function PageHead({
  eyebrow, title, note, actions, status,
}: {
  eyebrow?: string;
  title: string;
  note?: string;
  actions?: ReactNode;
  status?: { label: string; tone: "go" | "neutral" | "signal" };
}) {
  return (
    <header className="border-b border-line bg-panel px-5 py-5 md:px-8">
      <div className="mx-auto flex max-w-6xl flex-wrap items-end justify-between gap-4">
        <div className="min-w-0">
          {eyebrow ? (
            <p className="mb-1 text-[11px] font-medium uppercase tracking-[0.12em] text-ink-faint">{eyebrow}</p>
          ) : null}
          <div className="flex flex-wrap items-center gap-2.5">
            <h1 className="text-[22px] font-semibold tracking-[-0.02em]">{title}</h1>
            {status ? <Pill tone={status.tone} dot>{status.label}</Pill> : null}
          </div>
          {note ? <p className="mt-1.5 max-w-2xl text-[13px] leading-relaxed text-ink-soft">{note}</p> : null}
        </div>
        {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
      </div>
    </header>
  );
}
