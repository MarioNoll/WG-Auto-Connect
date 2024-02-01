package de.marionoll.wgautoconnect.home.ui.guide

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
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
        addStringAnnotation(
            tag = WIRE_GUARD_TEXT_KEY,
            annotation = WIRE_GUARD_TEXT_KEY,
            start = wireGuardTextStartIndex,
            end = wireGuardTextEndIndex,
        )
        addStyle(
            style = SpanStyle(
                color = Color("#0000EE".toColorInt()),
                textDecoration = TextDecoration.Underline
            ),
            start = wireGuardTextStartIndex,
            end = wireGuardTextStartIndex + WIRE_GUARD_TEXT_KEY.length,
        )
    }

    ClickableText(
        modifier = Modifier.fillMaxWidth(),
        text = annotatedString,
        style = TextStyle.Default.copy(
            color = LocalContentColor.current,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
        ),
        onClick = { offset ->
            annotatedString
                .getStringAnnotations(offset, offset)
                .firstOrNull()
                ?.run {
                    onEvent(Event.WireGuardClick)
                }
        })
}
