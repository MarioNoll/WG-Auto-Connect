package de.marionoll.wgautoconnect.home.ui.guide

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.graphics.toColorInt
import de.marionoll.wgautoconnect.R
import de.marionoll.wgautoconnect.home.Event

private const val WIRE_GUARD_TEXT_KEY = "WireGuard"

@Composable
fun GuideTitle(
    onEvent: (Event) -> Unit,
) {
    val guideTitleText = stringResource(id = R.string.home_guide_title)
    val wireGuardTextStartIndex = guideTitleText.indexOf(WIRE_GUARD_TEXT_KEY)
    val wireGuardTextEndIndex = wireGuardTextStartIndex + WIRE_GUARD_TEXT_KEY.length

    val annotatedString = buildAnnotatedString {
        append(guideTitleText)
        addLink(
            LinkAnnotation.Clickable(
                tag = WIRE_GUARD_TEXT_KEY,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = Color("#0000EE".toColorInt()),
                        textDecoration = TextDecoration.Underline,
                    ),
                ),
                linkInteractionListener = { onEvent(Event.WireGuardClick) },
            ),
            start = wireGuardTextStartIndex,
            end = wireGuardTextEndIndex,
        )
    }

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = annotatedString,
        style = TextStyle.Default.copy(
            color = LocalContentColor.current,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
        ),
    )
}
