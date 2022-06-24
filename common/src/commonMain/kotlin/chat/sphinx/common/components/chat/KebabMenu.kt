package chat.sphinx.common.components.chat


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun KebabMenu(
    contentDescription: String,
    onClick: () -> Unit
) {
    Icon(
        Icons.Default.MoreVert,
        contentDescription,
        tint= Color(0xff556171),
        modifier = Modifier.height(70.dp)
            .width(30.dp)
            .padding(start = 1.dp, top = 25.dp, end = 1.dp, bottom = 25.dp)
            .clickable(
                onClickLabel = contentDescription,
                onClick = onClick
            ),
    )
}