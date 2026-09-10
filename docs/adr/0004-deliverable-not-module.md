# The eighteen things a brand system is made of are Deliverables, not Modules

They were called modules until the glossary was written, at which point the
collision was obvious: `module` already means a unit of code, and
`ModuleView` — a code module — rendered a `Module` — a brand deliverable. The
domain concept was renamed to **Deliverable** throughout (types, files, the
`?deliverable=` query parameter, and the UI copy), leaving `module` free for
its ordinary meaning.

## Consequences

Projects saved before the rename stored them under `modules`; the store reads
either field on hydrate so existing work survives. That migration can be
removed once no old projects are plausibly in play.
