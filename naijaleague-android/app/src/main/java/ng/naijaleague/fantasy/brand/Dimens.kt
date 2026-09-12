package ng.naijaleague.fantasy.brand

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Spacing and sizing — brand system v1.1, sections 02 and 13.
 *
 * "Dense, not cramped." This audience reads league tables for pleasure. They do
 * not need one player per card with generous whitespace; they need fifteen rows
 * they can scan. Patronising layout reads as a product built by people who do
 * not watch football.
 */
object BrandDimens {
    /** Minimum tap target, whatever the type size inside it. */
    val MinTapTarget = 44.dp

    /** The side gutter. Set once, on the screen scaffold. */
    val Gutter = 16.dp

    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 12.dp
    val SpaceLg = 16.dp
    val SpaceXl = 24.dp
    val SpaceXxl = 32.dp

    /** Squad rows are dense on purpose: fifteen have to fit and stay scannable. */
    val SquadRowHeight = 56.dp

    /**
     * Cards mean "a separate thing you can act on" — a player, a fixture, a
     * league. Tables, rules pages and lists are type on the ground (§13).
     */
    val CardRadius = 14.dp
    val ChipRadius = 8.dp
    val ButtonRadius = 12.dp
    val ButtonHeight = 52.dp

    /**
     * The v1.1 audience change moved the floor. With the core user at 16–60,
     * body copy never ships below 15sp, and 14sp is for secondary information
     * only — never rules, never anything involving money.
     */
    val MinBodySize = 15.sp

    /** The deadline strip is permanent furniture, not a banner. */
    val DeadlineStripHeight = 40.dp
}
