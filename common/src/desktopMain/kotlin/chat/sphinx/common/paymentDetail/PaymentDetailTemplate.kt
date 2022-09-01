package chat.sphinx.common.paymentDetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.sphinx.common.components.PhotoFileImage
import chat.sphinx.common.components.PhotoUrlImage
import chat.sphinx.common.viewmodel.chat.ChatViewModel
import chat.sphinx.common.viewmodel.chat.payment.PaymentViewModel
import chat.sphinx.wrapper.PhotoUrl
import okio.Path.Companion.toOkioPath



@Composable
fun PaymentDetailTemplate(
    chatViewModel: ChatViewModel,
    viewModel: PaymentViewModel
) {
    Box(
        modifier = Modifier
            .width(400.dp)
            .height(550.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(10.dp)
            ).clickable {

            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.background(MaterialTheme.colorScheme.background).height(550.dp).width(400.dp)
        ) {
            Text("")
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(16.dp).clickable {
                        chatViewModel.toggleChatActionsPopup(
                            ChatViewModel.ChatActionsMode.SEND_AMOUNT, viewModel.getPaymentData()
                        )
                    }
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp).clickable {
                        chatViewModel.hideChatActionsPopup()
                    }
                )
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.chatPaymentState.amount?.toString() ?: "" ,
                    color = MaterialTheme.colorScheme.tertiary, fontSize = 40.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sat", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = viewModel.chatPaymentState.message,
                color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            val testList by viewModel.paymentTemplateList.collectAsState()

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = rememberLazyListState()
            ) {
                item {
                    Spacer(modifier = Modifier.width(100.dp))
                }
                testList?.size?.let {
                    items(it) { index ->
                        testList?.get(index)?.localFile?.toOkioPath()?.let { path ->
                            PhotoFileImage(
                                photoFilepath = path,
                                modifier = Modifier.height(250.dp).width(200.dp).padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = rememberLazyListState()
            ) {
                item {
                    Spacer(modifier = Modifier.width(100.dp))
                }
                items(10) {
                    PhotoUrlImage(
                        PhotoUrl("https://picsum.photos/200/300"), modifier = Modifier
                            .size(60.dp).padding(8.dp)
                            .clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {},
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(0.5f).height(50.dp)
            ) {
                Text("CONFIRM", color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
            }

        }
    }
}