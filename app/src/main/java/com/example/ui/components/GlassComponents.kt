package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderWidth: Float = 1f,
    content: @Composable ColumnScope.() -> Unit
) {
    val maybeClickable = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .background(
                color = SlateSurface,
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = borderWidth.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .then(maybeClickable)
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tag: String? = null
) {
    val buttonModifier = if (tag != null) modifier.testTag(tag) else modifier

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(),
        modifier = buttonModifier
            .height(50.dp)
            .minimumInteractiveComponentSize()
    ) {
        val gradient = Brush.linearGradient(
            colors = if (enabled) listOf(AccentGradientStart, AccentGradientEnd) else listOf(SlateSurface, SlateSurface)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) Color.White else TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .background(
                        Brush.verticalGradient(listOf(NeonPurple, BrightBlue)),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                fontSize = 14.sp,
                color = NeonPurple,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { onActionClick() }
            )
        }
    }
}
