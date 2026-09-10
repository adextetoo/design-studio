# Project state stays in the browser

There is no server, no account and no sync: a project lives in `localStorage`
and the app tells the user so in the sidebar. That buys a tool anyone can open
and use immediately with a client's confidential positioning in it, and it
costs multi-device access, collaboration, and any recovery if the browser data
is cleared.

## Consequences

The published Artifact stores per viewer and per origin. Moving to shared
storage later is a real capability change, not a refactor — and it makes the
"nothing leaves the device" line in the UI false, so that copy has to change
in the same commit.
