"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { DELIVERABLES } from "@/data/deliverables";
import { buildBrandMd, buildDesignMd } from "@/lib/markdown";
import { saveMarkdown } from "@/lib/download";
import { useStudio } from "@/lib/store";
import { PageHead } from "@/components/Shell";
import { Button, Kicker, Panel, Pill, ProgressBar, SectionHead } from "@/components/ui";
import { useToast } from "@/components/Toast";

type FileKey = "brand" | "design";

export default function HandoffPage() {
  const { project, approvedCount } = useStudio();
  const toast = useToast();
  const [open, setOpen] = useState<FileKey>("brand");
  const [copied, setCopied] = useState<FileKey | null>(null);
  const [saving, setSaving] = useState<FileKey | null>(null);
  const [saveNote, setSaveNote] = useState<{ key: FileKey; text: string } | null>(null);

  const files = useMemo(
    () => ({
      brand: { name: "Brand.md", body: buildBrandMd(project) },
      design: { name: "Design.md", body: buildDesignMd(project) },
    }),
    [project],
  );

  const pending = DELIVERABLES.filter((m) => project.deliverables[m.id].status !== "approved");
  const slug = (project.brief.brandName || "brand").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

  const download = async (key: FileKey) => {
    const file = files[key];
    setSaving(key);
    setSaveNote(null);
    const outcome = await saveMarkdown(`${slug}-${file.name}`, file.body);
    setSaving(null);
    if (outcome.status === "saved") {
      toast.show(`${file.name} saved`, { detail: `${approvedCount} approved deliverables.`, tone: "go" });
      return;
    }
    setSaveNote({
      key,
      text:
        outcome.status === "declined"
          ? "Not saved. Nothing was written to your machine."
          : outcome.status === "unavailable"
            ? "Saving files is off in this view. Use Copy to clipboard instead — the whole file is on the clipboard."
            : outcome.message,
    });
  };

  const copy = async (key: FileKey) => {
    try {
      await navigator.clipboard.writeText(files[key].body);
      setCopied(key);
      toast.show(`${files[key].name} copied`, { detail: "Paste it into an assistant to brief it on the brand." });
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
        note="Two Markdown files built from the approved deliverables only. Hand them to a developer, a freelancer, or paste them into an assistant as a system prompt so it is briefed on the brand before it writes a word."
        status={{
          label: `${approvedCount} of ${DELIVERABLES.length} approved`,
          tone: approvedCount === DELIVERABLES.length ? "go" : "neutral",
        }}
      />

      <div className="mx-auto max-w-6xl space-y-6 px-5 py-6 md:px-8">
        <Panel>
          <SectionHead
            title="What goes in"
            note="Approving a deliverable is the only thing that puts it in these files. Withdraw the approval and it comes straight back out."
          />
          <ProgressBar value={approvedCount} total={DELIVERABLES.length} />
          <div className="mt-4 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
            {DELIVERABLES.map((m) => {
              const status = project.deliverables[m.id].status;
              return (
                <Link
                  key={m.id}
                  href={{ pathname: "/studio", query: { deliverable: m.id } }}
                  className={`flex items-center justify-between gap-2 rounded-md border px-3 py-2 text-body transition-colors ${
                    status === "approved" ? "border-go/30 bg-go-soft" : "border-line hover:bg-sunken"
                  }`}
                >
                  <span className="truncate">{m.title}</span>
                  <span className="shrink-0 text-micro text-ink-faint">
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
            <p className="mt-4 rounded-md bg-sunken px-3 py-2 text-body leading-relaxed text-ink-soft">
              {pending.length} deliverable{pending.length === 1 ? " is" : "s are"} still unapproved, so {pending.length === 1 ? "it is" : "they are"} listed
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
                  <Button variant="primary" onClick={() => download(key)} disabled={saving === key}>
                    <span aria-hidden>↓</span> {saving === key ? "Saving…" : `Download ${file.name}`}
                  </Button>
                  <Button onClick={() => copy(key)}>{copied === key ? "Copied" : "Copy to clipboard"}</Button>
                  <Button variant="ghost" onClick={() => setOpen(key)}>
                    Preview
                  </Button>
                </div>
                {saveNote?.key === key ? (
                  <p role="status" className="mt-3 rounded-md bg-sunken px-3 py-2 text-body leading-relaxed text-ink-soft">
                    {saveNote.text}
                  </p>
                ) : null}
                <p className="mt-3 text-micro tabular-nums text-ink-faint">
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
                  className={`rounded px-3 py-1 text-body transition-colors ${
                    open === key ? "bg-ink text-panel" : "text-ink-soft hover:text-ink"
                  }`}
                >
                  {files[key].name}
                </button>
              ))}
            </div>
            <Kicker>Exactly what downloads</Kicker>
          </div>
          <pre className="max-h-[34rem] overflow-auto px-5 py-4 font-mono text-body leading-relaxed text-ink-soft">
            {files[open].body}
          </pre>
        </Panel>

        <Panel>
          <SectionHead title="How to use these" note="Written to be picked up by a person or an assistant with no further explanation." />
          <ul className="space-y-2.5 text-lead leading-relaxed text-ink-soft">
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
