"use client";

import type { AnchorHTMLAttributes, ReactNode } from "react";
import { hrefToPath } from "./router";

type LinkProps = Omit<AnchorHTMLAttributes<HTMLAnchorElement>, "href"> & {
  href: unknown;
  children: ReactNode;
};

/** Stands in for `next/link` in the standalone build. */
export default function Link({ href, children, ...rest }: LinkProps) {
  return (
    <a href={`#${hrefToPath(href)}`} {...rest}>
      {children}
    </a>
  );
}
