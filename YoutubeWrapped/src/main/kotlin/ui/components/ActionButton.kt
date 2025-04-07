package ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
        border = BorderStroke(2.dp, Color.Black),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold)
    }
}


@Preview
@Composable
fun PreviewActionButton() {
    ActionButton(title = "Click Me", onClick = {})
}
