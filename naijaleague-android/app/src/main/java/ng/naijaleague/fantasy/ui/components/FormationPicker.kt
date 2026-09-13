package ng.naijaleague.fantasy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.LocalAnimationsEnabled
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandMotion
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.rules.Formations
import ng.naijaleague.fantasy.rules.Squad

/**
 * The formation picker.
 *
 * A GLASS PANEL OVER THE PITCH, not a new screen. Changing shape is a decision
 * you make while looking at the team — send the manager somewhere else and they
 * lose the thing they were deciding about. So the pitch stays visible and dimmed
 * underneath, and the panel sits over it.
 *
 * The panel is translucent rather than a flat sheet because the pitch behind it
 * is information: the green and the eleven tokens still read through it, so it
 * is obvious what is about to change. Where the platform can blur a backdrop
 * (Android 12 and up) the caller blurs the pitch as well; below that the scrim
 * carries it alone, which is why the scrim is weighted to work on its own.
 *
 * THE CAROUSEL LOOPS, and that is the whole reason it is a carousel. Eight
 * shapes do not fit on a phone's width, and a strip that stops at both ends
 * makes a manager drag, hit a wall, and drag back. Looping means one direction
 * of travel reaches everything — hold, drag, and keep going.
 *
 * It snaps. A free-scrolling strip leaves a shape half off the edge and the
 * manager has to nudge it straight before tapping; snapping settles each one
 * under the thumb by itself. That is what makes it feel like a dial rather than
 * a list on its side.
 */
@Composable
fun FormationPicker(
    squad: Squad,
    current: String,
    onPick: (Formations.Formation) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalBrandPalette.current
    val shapes = remember { Formations.legal }
    val reachable = remember(squad) { Formations.reachableFrom(squad).toSet() }

    // A very large count standing in for an endless one, entered in the middle,
    // so a manager can drag either way for as long as they like without ever
    // reaching an end. The modulo is what makes it a loop.
    val repeats = 1_000
    val startIndex = remember(shapes) {
        val middle = repeats / 2 * shapes.size
        val currentOffset = shapes.indexOfFirst { it.label == current }.coerceAtLeast(0)
        middle + currentOffset
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(Unit) { listState.scrollToItem(startIndex) }

    // The panel rises. A sheet that simply exists on the frame after a tap reads
    // as a mis-render; a short rise reads as an answer to the tap. It is the
    // brand enter curve — fast out, long settle, no bounce — and it is the same
    // 240ms the blur behind it takes, so the two land together.
    val animate = LocalAnimationsEnabled.current
    var entered by remember { mutableStateOf(!animate) }
    LaunchedEffect(Unit) { entered = true }
    val appear by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = if (animate) BrandMotion.enter() else snap(),
        label = "formationPickerEnter"
    )

    Box(Modifier.fillMaxSize()) {
        // The scrim. Tapping it dismisses, which is the gesture everybody tries
        // first; it carries no ripple because it is not a button.
        Box(
            Modifier
                .fillMaxSize()
                .background(BrandColor.NightPitch.copy(alpha = 0.62f * appear))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = ((1f - appear) * 28f).dp)
                .alpha(appear)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                // Translucent, so the pitch reads through and it is obvious what
                // is about to change.
                .background(palette.raised.copy(alpha = 0.88f))
                .border(
                    1.dp,
                    // The lit top edge that makes glass look like glass.
                    Color.White.copy(alpha = 0.10f),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .navigationBarsPadding()
                .padding(bottom = BrandDimens.SpaceLg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            // The grab handle. Says "this panel moves" before anyone touches it.
            Box(
                Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.inkDim.copy(alpha = 0.55f))
            )
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            SectionLabel("Formation")
            Spacer(Modifier.height(BrandDimens.SpaceXs))
            Text(
                "Hold and drag. Your eleven rearrange themselves; nobody is sold.",
                style = BrandType.InterfaceAndGuidance.bodySmall,
                color = palette.inkDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = BrandDimens.Gutter)
            )
            Spacer(Modifier.height(BrandDimens.SpaceLg))

            LazyRow(
                state = listState,
                flingBehavior = fling,
                horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceMd),
                contentPadding = PaddingValues(horizontal = BrandDimens.Gutter)
            ) {
                items(repeats * shapes.size) { index ->
                    val shape = shapes[index % shapes.size]
                    FormationChoice(
                        shape = shape,
                        selected = shape.label == current,
                        playable = shape in reachable,
                        onClick = { if (shape in reachable) onPick(shape) }
                    )
                }
            }
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            BrandButtonSecondary(
                label = "Keep $current",
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = BrandDimens.Gutter)
            )
        }
    }
}

/**
 * One shape on the dial: the label, and the shape itself drawn as dots.
 *
 * The diagram is the point. "4-4-2" and "4-3-3" are two numbers apart on paper
 * and a different team on grass, and a row of dots says which in less time than
 * reading takes.
 */
@Composable
private fun FormationChoice(
    shape: Formations.Formation,
    selected: Boolean,
    playable: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalBrandPalette.current
    val edge = when {
        selected -> palette.accent
        !playable -> palette.rule
        else -> Color.White.copy(alpha = 0.12f)
    }
    Column(
        Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(
                if (selected) palette.accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f)
            )
            .border(if (selected) 2.dp else 1.dp, edge, RoundedCornerShape(BrandDimens.CardRadius))
            .clickable(enabled = playable, onClick = onClick)
            .padding(vertical = BrandDimens.SpaceMd),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShapeDiagram(shape, dim = !playable)
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        Text(
            shape.label,
            style = BrandType.ScoreAndData.data,
            color = when {
                selected -> palette.accent
                playable -> palette.ink
                else -> palette.inkDim
            }
        )
        if (!playable) {
            Text(
                "not in your 15",
                style = BrandType.InterfaceAndGuidance.label,
                color = palette.inkDim
            )
        }
    }
}

/** The shape as four rows of dots, keeper at the back. */
@Composable
private fun ShapeDiagram(shape: Formations.Formation, dim: Boolean) {
    val palette = LocalBrandPalette.current
    val tint = if (dim) palette.inkDim else palette.accent
    Canvas(Modifier.size(width = 68.dp, height = 44.dp)) {
        val rows = listOf(1, shape.defenders, shape.midfielders, shape.forwards)
        val rowGap = size.height / rows.size
        val radius = size.minDimension * 0.055f
        rows.forEachIndexed { rowIndex, count ->
            val y = rowGap * rowIndex + rowGap / 2f
            val slot = size.width / (count + 1)
            repeat(count) { i ->
                drawCircle(
                    color = if (rowIndex == 0) tint.copy(alpha = 0.55f) else tint,
                    radius = radius,
                    center = Offset(slot * (i + 1), y)
                )
            }
        }
    }
}
