/**
 * Saving a file, in both places this app runs.
 *
 * As a normal web page a blob plus an anchor is all it takes. Published as an
 * Artifact the page is framed and that anchor is inert, so the save has to go
 * through the platform's `downloads` capability, which shows the viewer a
 * confirmation they can decline. Detecting which world we are in is the whole
 * job: `window.claude.use` only exists on the platform, so its presence — not
 * the outcome of a call — decides which path is the real one.
 */

type Downloads = { save(request: { filename: string; data: string | Blob }): Promise<{ status: string }> };

interface ClaudeHost {
  use?: (name: string) => Promise<Downloads | null>;
}

declare global {
  interface Window {
    claude?: ClaudeHost;
  }
}

export type SaveOutcome =
  | { status: "saved" }
  | { status: "declined" }
  /** The platform cannot save here; the viewer should copy instead. */
  | { status: "unavailable" }
  | { status: "error"; message: string };

function anchorDownload(filename: string, body: string): SaveOutcome {
  const url = URL.createObjectURL(new Blob([body], { type: "text/markdown;charset=utf-8" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
  return { status: "saved" };
}

export async function saveMarkdown(filename: string, body: string): Promise<SaveOutcome> {
  const host = typeof window === "undefined" ? undefined : window.claude;

  // A plain browser: the anchor is the only path, and it works.
  if (!host?.use) return anchorDownload(filename, body);

  // On the platform the anchor does nothing, so there is no fallback to take.
  let downloads: Downloads | null = null;
  try {
    downloads = await host.use("downloads");
  } catch {
    downloads = null;
  }
  if (!downloads) return { status: "unavailable" };

  try {
    await downloads.save({ filename, data: body });
    return { status: "saved" };
  } catch (error) {
    const code = (error as { code?: string } | null)?.code;
    if (code === "declined") return { status: "declined" };
    if (code === "rate_limited") {
      return { status: "error", message: "A save prompt is already open. Try again in a moment." };
    }
    if (code === "unavailable" || code === "not_granted" || code === "capability_disabled" || code === "capability_removed") {
      return { status: "unavailable" };
    }
    return { status: "error", message: (error as { message?: string } | null)?.message ?? "The file could not be saved." };
  }
}
