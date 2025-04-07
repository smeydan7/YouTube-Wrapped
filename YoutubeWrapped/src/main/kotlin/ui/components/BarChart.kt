package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarChartComponent(
    title: String,
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {

            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val density = LocalDensity.current
                val canvasWidthPx = with(density) { maxWidth.toPx() }
                val canvasHeightPx = with(density) { maxHeight.toPx() }
                val maxBarHeightPx = canvasHeightPx - with(density) { 20.dp.toPx() }
                val barWidthPx = canvasWidthPx / (data.size * 1.5f)
                val maxValue = data.maxOrNull()?.toFloat() ?: 1f


                Canvas(modifier = Modifier.matchParentSize()) {
                    data.forEachIndexed { index, value ->
                        val barHeightPx = (value / maxValue) * maxBarHeightPx
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(index * barWidthPx * 1.5f, canvasHeightPx - barHeightPx),
                            size = Size(barWidthPx, barHeightPx)
                        )
                    }
                }

                data.forEachIndexed { index, value ->
                    val barHeightPx = (value / maxValue) * maxBarHeightPx
                    val xPosPx = index * barWidthPx * 1.5f + barWidthPx / 2f
                    val yPosPx = canvasHeightPx - barHeightPx - 8f
                    val xPosDp = with(density) { xPosPx.toDp() }
                    val yPosDp = with(density) { yPosPx.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = xPosDp - 10.dp, y = yPosDp - 10.dp)
                    ) {
                        Text(text = value.toString(), fontSize = 12.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}
