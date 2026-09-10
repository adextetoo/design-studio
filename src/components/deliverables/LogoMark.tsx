"use client";

import type { MarkShape } from "@/lib/types";

/**
 * Constructed marks.
 *
 * Drawn as geometry rather than shipped as images so a route can be recoloured
 * with the approved palette and inspected at any size — which is the only way
 * to judge whether a mark survives being small.
 */
export function LogoMark({
  shape, size = 96, color = "currentColor", letter = "A",
}: { shape: MarkShape; size?: number; color?: string; letter?: string }) {
  const common = { fill: color };
  return (
    <svg
      viewBox="0 0 100 100"
      width={size}
      height={size}
      role="img"
      aria-label={`${shape} mark`}
      style={{ display: "block" }}
    >
      {shape === "arc" && (
        <path
          d="M50 6a44 44 0 1 1-31.1 12.9L30.2 30.2A28 28 0 1 0 50 22Z"
          {...common}
        />
      )}
      {shape === "aperture" && (
        <path d="M14 14h72v72H14Zm14 14v44h44V50H50V28Z" fillRule="evenodd" {...common} />
      )}
      {shape === "monogram" && (
        <text
          x="50" y="50" textAnchor="middle" dominantBaseline="central"
          fontSize="62" fontWeight="700" letterSpacing="-3" {...common}
        >
          {letter}
        </text>
      )}
      {shape === "chevron" && (
        <path d="M50 10 88 76H70L50 40 30 76H12Z M50 52l14 24H36Z" fillRule="evenodd" {...common} />
      )}
      {shape === "orbit" && (
        <g {...common}>
          <path d="M50 8c14 0 25 19 25 42S64 92 50 92 25 73 25 50 36 8 50 8Zm0 10c-8 0-15 14-15 32s7 32 15 32 15-14 15-32-7-32-15-32Z" />
          <path d="M50 8c14 0 25 19 25 42S64 92 50 92 25 73 25 50 36 8 50 8Z" opacity=".35" transform="rotate(90 50 50)" />
        </g>
      )}
      {shape === "grid" && (
        <g {...common}>
          {[0, 1, 2, 3, 4].map((row) =>
            [0, 1, 2, 3, 4].map((col) => {
              const centre = Math.abs(row - 2) <= 1 && Math.abs(col - 2) <= 1;
              return (
                <circle
                  key={`${row}-${col}`}
                  cx={18 + col * 16}
                  cy={18 + row * 16}
                  r={centre ? 6.5 : 3.5}
                />
              );
            }),
          )}
        </g>
      )}
      {shape === "seal" && (
        <g {...common}>
          <path d="M50 6a44 44 0 1 1 0 88 44 44 0 0 1 0-88Zm0 9a35 35 0 1 0 0 70 35 35 0 0 0 0-70Z" />
          <rect x="26" y="47" width="48" height="6" />
        </g>
      )}
      {shape === "cut" && <path d="M14 14h72v72H14Zm18 54h36V32Z" fillRule="evenodd" {...common} />}
    </svg>
  );
}
