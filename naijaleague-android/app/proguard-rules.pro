# The rules engine is reflection-free, so nothing needs keeping for it.
# Keep the domain model names readable in crash reports from the field —
# a scoring dispute is the one bug report we must always be able to read.
-keepnames class ng.naijaleague.fantasy.rules.** { *; }
