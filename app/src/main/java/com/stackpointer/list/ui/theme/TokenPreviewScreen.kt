package com.stackpointer.list.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Renders every design token so it can be compared against design/screenshots/ side by side,
 * per BUILD_PLAN.md's M1. Not a real app screen.
 */
@Composable
fun TokenPreviewScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ColorSwatches()
            TypeSpecimens()
            ShapeSwatches()
        }
    }
}

@Composable
private fun ColorSwatches() {
    val scheme = MaterialTheme.colorScheme
    val roles = listOf(
        "primary" to scheme.primary, "onPrimary" to scheme.onPrimary,
        "primaryContainer" to scheme.primaryContainer, "onPrimaryContainer" to scheme.onPrimaryContainer,
        "secondary" to scheme.secondary, "secondaryContainer" to scheme.secondaryContainer,
        "onSecondaryContainer" to scheme.onSecondaryContainer,
        "tertiary" to scheme.tertiary, "tertiaryContainer" to scheme.tertiaryContainer,
        "onTertiaryContainer" to scheme.onTertiaryContainer,
        "error" to scheme.error, "errorContainer" to scheme.errorContainer,
        "onErrorContainer" to scheme.onErrorContainer,
        "surface" to scheme.surface, "surfaceContainerLowest" to scheme.surfaceContainerLowest,
        "surfaceContainerLow" to scheme.surfaceContainerLow, "surfaceContainer" to scheme.surfaceContainer,
        "surfaceContainerHigh" to scheme.surfaceContainerHigh,
        "surfaceContainerHighest" to scheme.surfaceContainerHighest,
        "onSurface" to scheme.onSurface, "onSurfaceVariant" to scheme.onSurfaceVariant,
        "outline" to scheme.outline, "outlineVariant" to scheme.outlineVariant,
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Colour", style = MaterialTheme.typography.titleLarge)
        roles.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (name, color) -> ColorSwatch(name, color, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .background(color, RoundedCornerShape(8.dp)),
        )
        Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun TypeSpecimens() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Type", style = MaterialTheme.typography.titleLarge)
        Text("Display Small", style = MaterialTheme.typography.displaySmall)
        Text("Headline Large", style = MaterialTheme.typography.headlineLarge)
        Text("Headline Medium emphasized", style = MaterialTheme.typography.headlineMedium)
        Text("Title Large", style = MaterialTheme.typography.titleLarge)
        Text("Title Medium", style = MaterialTheme.typography.titleMedium)
        Text("Title Medium emphasized", style = TitleMediumEmphasized)
        Text("Body Large", style = MaterialTheme.typography.bodyLarge)
        Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
        Text("Label Large", style = MaterialTheme.typography.labelLarge)
        Text("Label Large emphasized", style = LabelLargeEmphasized)
        Text("Label Medium", style = MaterialTheme.typography.labelMedium)
        Text("Label Small", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ShapeSwatches() {
    val shapes = MaterialTheme.shapes
    val entries = listOf(
        "extraSmall (4)" to shapes.extraSmall, "small (8)" to shapes.small,
        "medium (12)" to shapes.medium, "large (16)" to shapes.large,
        "extraLarge (28)" to shapes.extraLarge, "full" to FullShape,
        "navPill (32)" to NavPillShape,
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Shape", style = MaterialTheme.typography.titleLarge)
        entries.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (name, shape) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape),
                        )
                        Text(name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun TokenPreviewScreenLightPreview() {
    DigitalListTheme(darkTheme = false, dynamicColor = false) {
        TokenPreviewScreen()
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TokenPreviewScreenDarkPreview() {
    DigitalListTheme(darkTheme = true, dynamicColor = false) {
        TokenPreviewScreen()
    }
}
