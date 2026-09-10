import type { Metadata } from "next";
import { StoreProvider } from "@/lib/store";
import { Shell } from "@/components/Shell";
import "./globals.css";

export const metadata: Metadata = {
  title: "Design Studio — brand systems, end to end",
  description:
    "Run discovery, build the strategy, design the identity and hand over Brand.md and Design.md from one workspace.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en-GB">
      <body>
        <StoreProvider>
          <Shell>{children}</Shell>
        </StoreProvider>
      </body>
    </html>
  );
}
