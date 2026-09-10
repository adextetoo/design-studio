"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { MODULES } from "@/data/modules";
import { buildBrandMd, buildDesignMd } from "@/lib/markdown";
import { useStudio } from "@/lib/store";
import { PageHead } from "@/components/Shell";
import { Button, Kicker, Panel, Pill, ProgressBar, SectionHead } from "@/components/ui";

type FileKey = "brand" | "design";

export default function HandoffPage() {
  const { project, approvedCount } = useStudio();
  const [open, setOpen] = useState<FileKey>("brand");
  const [copied, setCopied] = useState<FileKey | null>(null);

  const files = useMemo(
    () => ({
      brand: { name: "Brand.md", body: buildBrandMd(project) },
      design: { name: "Design.md", body: buildDesignMd(project) },
    }),
    [project],
  );

  const pending = MODULES.filter((m) => project.modules[m.id].status !== "approved");
  const slug = (project.brief.brandName || "brand").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

  const download = (key: FileKey) => {
    const file = files[key];
    const blob = new Blob([file.body], { type: "text/markdown;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${slug}-${file.name}`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  };

  const copy = async (key: FileKey) => {
    try {
      await navigator.clipboard.writeText(files[key].body);
      setCopied(key);
      window.setTimeout(() => setCopied(null), 1600);
    } catch {
      // Clipboard permission denied. The textarea below is still selectable.
    }
  };

  return (
    <>
      <PageHead
        eyebrow={`${project.brief.brandName || "Client"} · Handoff`}
        title="Brand.md and Design.md"
        note="Two Markdown files built from the approved modules only. Hand them to a developer, a freelancer, or paste them into an assistant as a system prompt so it is briefed on the brand before it writes a word."
        status={{
          label: `${approvedCount} of ${MODULES.length} approved`,
          tone: approvedCount === MODULES.length ? "go" : "neutral",
        }}
      />

      <div className="mx-auto max-w-6xl space-y-6 px-5 py-6 md:px-8">
        <Panel>
          <SectionHead
            title="What goes in"
            note="Approving a module is the only thing that puts it in these files. Withdraw the approval and it comes straight back out."
          />
          <ProgressBar value={approvedCount} total={MODULES.length} />
          <div className="mt-4 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
            {MODULES.map((m) => {
              const status = project.modules[m.id].status;
              return (
                <Link
                  key={m.id}
                  href={{ pathname: "/studio", query: { module: m.id } }}
                  className={`flex items-center justify-between gap-2 rounded-md border px-3 py-2 text-[13px] transition-colors ${
                    status === "approved" ? "border-go/30 bg-go-soft" : "border-line hover:bg-sunken"
                  }`}
                >
                  <span className="truncate">{m.title}</span>
                  <span className="shrink-0 text-[11px] text-ink-faint">
                    {status === "approved" ? (
                      <span className="text-go">✓ in {m.exports === "both" ? "both" : m.exports}</span>
                    ) : status === "draft" ? (
                      "draft"
                    ) : (
                      "—"
                    )}
                  </span>
                </Link>
              );
            })}
          </div>
          {pending.length > 0 ? (
            <p className="mt-4 rounded-md bg-sunken px-3 py-2 text-[12px] leading-relaxed text-ink-soft">
              {pending.length} module{pending.length === 1 ? " is" : "s are"} still unapproved, so {pending.length === 1 ? "it is" : "they are"} listed
              by name at the bottom of each file under &ldquo;Not yet approved&rdquo;. Whoever picks these up can see
              exactly how much of the system a human has signed off.
            </p>
          ) : null}
        </Panel>

        <div className="grid gap-4 lg:grid-cols-2">
          {(["brand", "design"] as FileKey[]).map((key) => {
            const file = files[key];
            const lines = file.body.split("\n").length;
            const words = file.body.split(/\s+/).filter(Boolean).length;
            return (
              <Panel key={key}>
                <SectionHead
                  title={file.name}
                  note={
                    key === "brand"
                      ? "Story, mission, vision, offering, market, audience, competition, differentiation, exposé, voice."
                      : "Look and feel, logo, colour, typography, packaging, marketing, social and web — with CSS tokens at the top."
                  }
                  action={<Pill>{lines} lines</Pill>}
                />
                <div className="flex flex-wrap items-center gap-2">
                  <Button variant="primary" onClick={() => download(key)}>
                    <span aria-hidden>↓</span> Download {file.name}
                  </Button>
                  <Button onClick={() => copy(key)}>{copied === key ? "Copied" : "Copy to clipboard"}</Button>
                  <Button variant="ghost" onClick={() => setOpen(key)}>
                    Preview
                  </Button>
                </div>
                <p className="mt-3 text-[11px] tabular-nums text-ink-faint">
                  {words.toLocaleString("en-GB")} words · {(new Blob([file.body]).size / 1024).toFixed(1)} KB
                </p>
              </Panel>
            );
          })}
        </div>

        <Panel flush>
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-5 py-3">
            <div className="flex rounded-md border border-line p-0.5">
              {(["brand", "design"] as FileKey[]).map((key) => (
                <button
                  key={key}
                  type="button"
                  onClick={() => setOpen(key)}
                  aria-pressed={open === key}
                  className={`rounded px-3 py-1 text-[13px] transition-colors ${
                    open === key ? "bg-ink text-panel" : "text-ink-soft hover:text-ink"
                  }`}
                >
                  {files[key].name}
                </button>
              ))}
            </div>
            <Kicker>Exactly what downloads</Kicker>
          </div>
          <pre className="max-h-[34rem] overflow-auto px-5 py-4 font-mono text-[12px] leading-relaxed text-ink-soft">
            {files[open].body}
          </pre>
        </Panel>

        <Panel>
          <SectionHead title="How to use these" note="Written to be picked up by a person or an assistant with no further explanation." />
          <ul className="space-y-2.5 text-[14px] leading-relaxed text-ink-soft">
            {[
              "Paste Brand.md into an assistant as a system prompt before asking it to write anything for this client. It will stop inventing a voice.",
              "Commit both files next to the code. The copy and the product then move together, and nobody has to find the deck.",
              "Send Design.md to a developer with the CSS tokens at the top — colour values, type scale and stacks are already in a form they can paste.",
              "Re-export after any change in the studio rather than editing these by hand. They are generated files and will be overwritten.",
            ].map((line, i) => (
              <li key={i} className="flex gap-2.5">
                <span className="mt-2 size-1 shrink-0 rounded-full bg-ink-faint" aria-hidden />
                {line}
              </li>
            ))}
          </ul>
        </Panel>
      </div>
    </>
  );
}
