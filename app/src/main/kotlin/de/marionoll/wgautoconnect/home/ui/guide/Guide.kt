package de.marionoll.wgautoconnect.home.ui.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.marionoll.wgautoconnect.R
import de.marionoll.wgautoconnect.home.Event

@Composable
fun Guide(
    onEvent: (Event) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GuideTitle(onEvent = onEvent)

        Spacer(modifier = Modifier.size(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            guideItems.forEach { guideItem ->
                GuideStepItem(
                    text = guideItem,
                )
            }
        }
    }
}

private val guideItems: List<Int>
    get() = listOf(
        R.string.home_guide_step_1,
        R.string.home_guide_step_2,
        R.string.home_guide_step_3,
    )
