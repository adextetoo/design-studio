"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { DELIVERABLE_IDS } from "@/data/deliverables";
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
      {/* data-sidebar, not a class, because the studio page uses <aside> again
          for its meta column and must not pick up the source-list material. */}
      <aside
        data-sidebar
        className="sticky top-0 hidden h-screen w-60 shrink-0 flex-col border-r border-line bg-panel md:flex"
      >
        <div className="flex items-center gap-2.5 px-4 py-4">
          <span
            className="grid size-7 shrink-0 place-items-center rounded-md bg-ink text-body font-bold text-panel"
            aria-hidden
          >
            ◆
          </span>
          <div className="min-w-0">
            <p className="truncate font-display text-lead leading-tight">Design Studio</p>
            <p className="truncate text-micro text-ink-faint">Brand systems, end to end</p>
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
                className={`mb-0.5 flex items-center gap-2.5 rounded-md px-2.5 py-1.5 text-body transition-colors ${
                  active ? "bg-sunken font-medium text-ink" : "text-ink-soft hover:bg-sunken/60 hover:text-ink"
                }`}
              >
                <span className="w-3.5 text-center text-micro text-ink-faint" aria-hidden>{item.glyph}</span>
                <span className="truncate">{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="mt-6 px-4">
          <p className="mb-2 font-data text-micro uppercase tracking-[0.1em] text-ink-faint">Client</p>
          <div className="flex items-center gap-2.5 rounded-md border border-line px-2.5 py-2">
            <span
              className="grid size-6 shrink-0 place-items-center rounded bg-ink text-micro font-bold text-panel"
              aria-hidden
            >
              {(project.brief.brandName || "?").charAt(0).toUpperCase()}
            </span>
            <div className="min-w-0">
              <p className="truncate text-body font-medium leading-tight">
                {project.brief.brandName || "Untitled"}
              </p>
              <p className="truncate text-micro text-ink-faint">{project.brief.sector || "No sector set"}</p>
            </div>
          </div>
        </div>

        <div className="mt-auto space-y-2 px-4 py-4 text-micro text-ink-faint">
          <div className="flex items-center justify-between">
            <span>Discovery</span>
            <span className="tabular-nums">{answered} / {STEPS.length}</span>
          </div>
          <div className="flex items-center justify-between">
            <span>Deliverables approved</span>
            <span className="tabular-nums">{approvedCount} / {DELIVERABLE_IDS.length}</span>
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
            className={`whitespace-nowrap rounded-md px-2.5 py-1.5 text-body ${
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
    <header data-toolbar className="border-b border-line bg-panel px-5 py-5 md:px-8">
      <div className="mx-auto flex max-w-6xl flex-wrap items-end justify-between gap-4">
        <div className="min-w-0">
          {eyebrow ? (
            <p className="mb-1.5 font-data text-micro uppercase tracking-[0.1em] text-ink-faint">{eyebrow}</p>
          ) : null}
          <div className="flex flex-wrap items-center gap-2.5">
            <h1 className="text-display tracking-[-0.025em]">{title}</h1>
            {status ? <Pill tone={status.tone} dot>{status.label}</Pill> : null}
          </div>
          {note ? <p className="mt-1.5 max-w-2xl text-body leading-relaxed text-ink-soft">{note}</p> : null}
        </div>
        {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
      </div>
    </header>
  );
}
