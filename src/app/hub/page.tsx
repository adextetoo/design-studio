"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { MODULES, MODULE_BY_ID } from "@/data/modules";
import type { ModuleId, PalettePayload, TypographyPayload } from "@/lib/types";
import { readableOn } from "@/lib/color";
import { useStudio } from "@/lib/store";
import { PageHead } from "@/components/Shell";
import { ModuleView } from "@/components/modules";
import { Button, EmptyState, Kicker, Pill } from "@/components/ui";

/**
 * The Brand Hub.
 *
 * What the client actually receives: the approved system as a single site,
 * ordered and numbered the way a printed brand guide is. Draft modules are
 * absent rather than shown greyed out — a guideline nobody signed is not a
 * guideline, and putting it here would make it look like one.
 */

const HUB_ORDER: ModuleId[] = [
  "story", "mission", "vision", "expose", "tone",
  "lookfeel", "logo", "palette", "typography",
  "packaging", "marketing", "social", "website",
  "offering", "market", "audience", "competition", "differentiation",
];

export default function HubPage() {
  const { project } = useStudio();
  const [openId, setOpenId] = useState<ModuleId | null>(null);

  const approved = useMemo(
    () => HUB_ORDER.filter((id) => project.modules[id].status === "approved"),
    [project.modules],
  );

  const palette = useMemo<PalettePayload | null>(() => {
    const s = project.modules.palette;
    const v = s.variants.find((x) => x.id === (s.approvedVariantId ?? s.activeVariantId));
    return v && v.payload.kind === "palette" ? v.payload.data : null;
  }, [project.modules.palette]);

  const typography = useMemo<TypographyPayload | null>(() => {
    const s = project.modules.typography;
    const v = s.variants.find((x) => x.id === (s.approvedVariantId ?? s.activeVariantId));
    return v && v.payload.kind === "typography" ? v.payload.data : null;
  }, [project.modules.typography]);

  const brandColor = palette?.swatches.find((s) => s.role === "primary")?.hex ?? "#17150F";
  const inkColor = palette?.swatches.find((s) => s.role === "ink")?.hex ?? "#17150F";
  const name = project.brief.brandName || "Untitled";
  const active = openId ?? approved[0] ?? null;
  const activeState = active ? project.modules[active] : null;
  const activeVariant = activeState?.variants.find((v) => v.id === activeState.approvedVariantId) ?? null;

  return (
    <>
      <PageHead
        eyebrow="Brand guidelines"
        title={`${name} Brand Hub`}
        note="The approved system, in the order a printed guide runs. Anything still in draft is deliberately missing — publishing an unapproved page is how a guideline gets ignored."
        status={{
          label: `${approved.length} of ${MODULES.length} pages live`,
          tone: approved.length > 0 ? "go" : "neutral",
        }}
        actions={
          <Link
            href="/handoff"
            className="rounded-md bg-ink px-3 py-1.5 text-[13px] font-medium text-panel hover:bg-ink/90"
          >
            Download the kit ›
          </Link>
        }
      />

      {approved.length === 0 ? (
        <div className="mx-auto max-w-2xl px-5 py-16 md:px-8">
          <EmptyState
            title="Nothing published yet"
            note="The hub fills up as modules get approved in the studio. Approve one and it appears here as a numbered page."
            action={
              <Link
                href="/studio"
                className="rounded-md bg-ink px-3 py-1.5 text-[13px] font-medium text-panel hover:bg-ink/90"
              >
                Open the design studio
              </Link>
            }
          />
        </div>
      ) : (
        <div className="mx-auto grid max-w-7xl gap-5 px-5 py-6 lg:grid-cols-[15rem_minmax(0,1fr)] md:px-8">
          <nav className="lg:sticky lg:top-6 lg:self-start" aria-label="Hub pages">
            <div
              className="mb-4 flex items-center gap-2.5 rounded-lg p-3"
              style={{ background: brandColor, color: readableOn(brandColor) }}
            >
              <span className="text-[16px]" aria-hidden>◆</span>
              <div className="min-w-0">
                <p
                  className="truncate text-[15px] font-semibold tracking-[-0.01em]"
                  style={{ fontFamily: typography?.primary.stack }}
                >
                  {name}
                </p>
                <p className="truncate text-[11px] opacity-70">
                  Updated {new Date(project.updatedAt).toLocaleDateString("en-GB", { month: "long", year: "numeric" })}
                </p>
              </div>
            </div>

            <ul>
              {approved.map((id, i) => (
                <li key={id}>
                  <button
                    type="button"
                    onClick={() => setOpenId(id)}
                    aria-current={id === active ? "true" : undefined}
                    className={`mb-0.5 flex w-full items-baseline gap-2.5 rounded-md px-2.5 py-1.5 text-left text-[13px] transition-colors ${
                      id === active ? "bg-sunken font-medium text-ink" : "text-ink-soft hover:bg-sunken/60"
                    }`}
                  >
                    <span className="tabular-nums text-ink-faint">{String(i + 1).padStart(2, "0")}</span>
                    <span className="truncate">{MODULE_BY_ID[id].title}</span>
                  </button>
                </li>
              ))}
            </ul>

{(() => {
              const unpublished = HUB_ORDER.filter((id) => project.modules[id].status !== "approved");
              if (unpublished.length === 0) {
                return (
                  <p className="mt-5 border-t border-line pt-4 text-[12px] leading-relaxed text-ink-faint">
                    Every module is approved and live. Nothing is being held back.
                  </p>
                );
              }
              return (
                <div className="mt-5 border-t border-line pt-4">
                  <Kicker className="mb-2">Not published</Kicker>
                  <ul className="space-y-0.5 text-[12px] text-ink-faint">
                    {unpublished.map((id) => (
                      <li key={id} className="flex items-center justify-between gap-2">
                        <span className="truncate">{MODULE_BY_ID[id].title}</span>
                        <span className="shrink-0">{project.modules[id].status === "draft" ? "draft" : "—"}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              );
            })()}
          </nav>

          <div className="min-w-0">
            {active && activeVariant ? (
              <article className="overflow-hidden rounded-lg border border-line bg-panel">
                <header
                  className="flex items-end justify-between gap-4 px-6 py-8"
                  style={{ background: inkColor, color: readableOn(inkColor) }}
                >
                  <span className="text-[clamp(28px,5vw,52px)] font-semibold leading-none tracking-[-0.03em] tabular-nums opacity-60">
                    {String(approved.indexOf(active) + 1).padStart(2, "0")}
                  </span>
                  <h2
                    className="text-[clamp(20px,3.4vw,34px)] font-semibold uppercase leading-none tracking-[0.02em]"
                    style={{ fontFamily: typography?.primary.stack }}
                  >
                    {MODULE_BY_ID[active].title}
                  </h2>
                </header>

                <div className="border-b border-line px-6 py-4">
                  <p className="max-w-2xl text-[14px] leading-relaxed text-ink-soft">
                    {MODULE_BY_ID[active].purpose}
                  </p>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <Pill tone="go" dot>Approved</Pill>
                    {activeState?.approvedAt ? (
                      <span className="text-[11px] text-ink-faint">
                        {new Date(activeState.approvedAt).toLocaleDateString("en-GB", {
                          day: "numeric", month: "long", year: "numeric",
                        })}
                      </span>
                    ) : null}
                  </div>
                </div>

                <div className="p-6">
                  <ModuleView
                    moduleId={active}
                    payload={activeVariant.payload}
                    palette={palette}
                    typeStack={typography?.primary.stack ?? null}
                    onPatch={() => {
                      /* The hub is a published view. Edits happen in the studio. */
                    }}
                  />
                </div>

                {activeState?.note ? (
                  <footer className="border-t border-line bg-sunken/50 px-6 py-4">
                    <Kicker className="mb-1">Studio note</Kicker>
                    <p className="max-w-2xl whitespace-pre-line text-[13px] leading-relaxed text-ink-soft">
                      {activeState.note}
                    </p>
                  </footer>
                ) : null}

                <footer className="flex flex-wrap items-center justify-between gap-3 border-t border-line px-6 py-3 text-[11px] text-ink-faint">
                  <span>
                    {name} · Brand hub · {new Date().getFullYear()}
                  </span>
                  <span>Page {approved.indexOf(active) + 1} of {approved.length}</span>
                </footer>
              </article>
            ) : null}

            <div className="mt-4 flex items-center justify-between">
              <Button
                onClick={() => {
                  const i = active ? approved.indexOf(active) : 0;
                  if (i > 0) setOpenId(approved[i - 1]);
                }}
                disabled={!active || approved.indexOf(active) === 0}
              >
                ‹ Previous page
              </Button>
              <Button
                onClick={() => {
                  const i = active ? approved.indexOf(active) : -1;
                  if (i > -1 && i < approved.length - 1) setOpenId(approved[i + 1]);
                }}
                disabled={!active || approved.indexOf(active) === approved.length - 1}
              >
                Next page ›
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
