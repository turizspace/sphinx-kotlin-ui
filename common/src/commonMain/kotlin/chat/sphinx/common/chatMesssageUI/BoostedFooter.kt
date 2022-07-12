package chat.sphinx.common.chatMesssageUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.sphinx.common.Res
import chat.sphinx.common.components.PhotoUrlImage
import chat.sphinx.common.models.ChatMessage
import chat.sphinx.platform.imageResource

@Composable
fun BoostedFooter(
    chatMessage: ChatMessage
) {
    val reaction = chatMessage.message.reactions?.get(0)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = imageResource(Res.drawable.ic_boost_green),
            contentDescription = "Boosted Icon",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${chatMessage.message.reactions?.sumOf { it.amount.value }} sats",
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier.padding(end = 25.dp).fillMaxWidth(0.85f),
            contentAlignment = Alignment.CenterEnd
        ) {

            chatMessage.message.reactions?.forEachIndexed { index, it ->
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .absoluteOffset((index * 10).dp, 0.dp)
                ) {
                    if(index<3)
                        PhotoUrlImage(
                            photoUrl = it.senderPic,
                            modifier = Modifier
                                .size(25.dp)
                                .clip(CircleShape),
                        )
                    else if(index==3){
                        Text((chatMessage.message.reactions?.size?:0-index).toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }


    }
}