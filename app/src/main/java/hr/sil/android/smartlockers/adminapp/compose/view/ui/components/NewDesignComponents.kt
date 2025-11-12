package hr.sil.android.smartlockers.adminapp.compose.view.ui.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import hr.sil.android.smartlockers.adminapp.R


import androidx.compose.material3.MaterialTheme as Material3

@Composable
fun NewDesignButton(
    paddingTop: Dp? = null,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    verticalTextPadding: Dp = 10.dp
) {
    val finalPaddingTop = if( paddingTop != null ) paddingTop else 32.dp
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = finalPaddingTop)
    ) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier
                .heightIn(min = 40.dp)
                .widthIn(min = 248.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colorResource(R.color.colorDarkAccent),
                contentColor = if (enabled) colorResource(R.color.colorDarkAccent) else colorResource(
                    R.color.colorDarkAccent
                ), //Material3.colorScheme.primary,
                disabledContainerColor = colorResource(R.color.colorPrimary)
            ),
            enabled = enabled
        ) {
            Text(
                text = title,
                color = if (enabled) Material3.colorScheme.onPrimary else if (!isSystemInDarkTheme()) Material3.colorScheme.primary.copy(
                    alpha = 0.12f
                ) else Material3.colorScheme.primary,
                style = Material3.typography.labelLarge,
                modifier = Modifier
                    .padding(vertical = verticalTextPadding)
            )
        }
    }
}
