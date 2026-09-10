import type { Metadata } from "next";
import { StoreProvider } from "@/lib/store";
import { Shell } from "@/components/Shell";
import "./globals.css";

export const metadata: Metadata = {
  title: "Design Studio — brand systems, end to end",
  description:
    "Run discovery, build the strategy, design the identity and hand over Brand.md and Design.md from one workspace.",
};

/*
 * The chrome faces plus every typeface the Typography deliverable can name.
 * A specimen that silently falls back to the viewer's system font is worthless
 * to a brand guide, so they are loaded rather than hoped for.
 */
const FONTS =
  "https://fonts.googleapis.com/css2" +
  "?family=Newsreader:wght@400;500;600" +
  "&family=Work+Sans:wght@400;500;600" +
  "&family=IBM+Plex+Mono:wght@400;500;600" +
  "&family=Manrope:wght@200;400;500;700" +
  "&family=Space+Grotesk:wght@300;400;500;700" +
  "&family=Poppins:wght@300;400;500;600" +
  "&family=Inter:wght@300;400;500;600;700" +
  "&family=Source+Sans+3:wght@400;600;700" +
  "&family=Lato:wght@300;400;700" +
  "&family=Source+Serif+4:wght@400;600;700" +
  "&family=Lora:wght@400;500" +
  "&family=Playfair+Display:wght@400;500;700" +
  "&family=Bodoni+Moda:wght@400;500;700" +
  "&family=Jost:wght@300;400" +
  "&family=Roboto+Slab:wght@400;500;700" +
  "&family=Zilla+Slab:wght@400;500;700" +
  "&family=JetBrains+Mono:wght@400;500;700" +
  "&display=swap";

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en-GB">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="" />
        <link rel="stylesheet" href={FONTS} />
      </head>
      <body>
        <StoreProvider>
          <Shell>{children}</Shell>
        </StoreProvider>
      </body>
    </html>
  );
}
