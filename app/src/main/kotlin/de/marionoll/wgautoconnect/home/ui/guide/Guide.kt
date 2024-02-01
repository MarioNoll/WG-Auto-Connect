package de.marionoll.wgautoconnect.home.ui.guide

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

        GuideStepItem(
            text = R.string.home_guide_step_1,
        )

        Spacer(modifier = Modifier.size(8.dp))

        GuideStepItem(
            text = R.string.home_guide_step_2,
        )

        Spacer(modifier = Modifier.size(8.dp))

        GuideStepItem(
            text = R.string.home_guide_step_3,
        )
    }
}
