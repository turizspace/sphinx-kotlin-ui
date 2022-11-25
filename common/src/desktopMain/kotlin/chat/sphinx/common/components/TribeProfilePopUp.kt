package chat.sphinx.common.components

import Roboto
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.sphinx.common.Res
import chat.sphinx.common.components.profile.Tabs
import chat.sphinx.common.components.profile.saveButton
import chat.sphinx.common.state.ContentState
import chat.sphinx.common.viewmodel.chat.ChatViewModel
import chat.sphinx.common.viewmodel.chat.payment.PaymentViewModel
import chat.sphinx.platform.imageResource
import chat.sphinx.response.LoadResponse
import chat.sphinx.utils.SphinxFonts
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.lightning.asFormattedString
import chat.sphinx.wrapper.message.media.isImage
import kotlinx.coroutines.launch
import theme.light_divider
import theme.primary_green
import theme.primary_red
import utils.deduceMediaType
import java.util.Stack

@Composable
fun TribeProfilePopUp(
    chatViewModel: ChatViewModel,
    paymentViewModel: PaymentViewModel
) {
    Box(
        modifier = Modifier
            .width(420.dp)
            .height(525.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp)
        ) {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.onSurfaceVariant),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PhotoUrlImage(
                        photoUrl = PhotoUrl("test"),
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                    Column() {
                        Text(
                            text = "TRIBE MEMBER",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Roboto,
                            fontSize = 10.sp
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "Stephanie",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Roboto,
                            fontSize = 22.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Column(
                            modifier = Modifier.height(40.dp).padding(top = 4.dp)
                        ) {
                            Text(
                                text = "This is an example for the description on tribe profile",
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = SphinxFonts.montserratFamily,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            SendSatsButton()
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(49.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Price to Meet:",
                    fontSize = 15.sp,
                    fontFamily = Roboto,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "150",
                        fontSize = 15.sp,
                        fontFamily = Roboto,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Divider(modifier = Modifier.fillMaxWidth(), color = light_divider)

            Row(
                modifier = Modifier.fillMaxWidth().height(49.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Coding languages:",
                    fontSize = 15.sp,
                    fontFamily = Roboto,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Python, C, Java, Kotlin, Swift, Goland,",
                    fontSize = 15.sp,
                    fontFamily = Roboto,
                    maxLines = 2,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Divider(modifier = Modifier.fillMaxWidth(), color = light_divider)

            Row(
                modifier = Modifier.fillMaxWidth().height(49.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Posts:",
                    fontSize = 15.sp,
                    fontFamily = Roboto,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "150",
                        fontSize = 15.sp,
                        fontFamily = Roboto,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Divider(modifier = Modifier.fillMaxWidth(), color = light_divider)

            Row(
                modifier = Modifier.fillMaxWidth().height(49.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Twitter:",
                    fontSize = 15.sp,
                    fontFamily = Roboto,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "150",
                        fontSize = 15.sp,
                        fontFamily = Roboto,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Divider(modifier = Modifier.fillMaxWidth(), color = light_divider)

            Row(
                modifier = Modifier.fillMaxWidth().height(49.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Github:",
                    fontSize = 15.sp,
                    fontFamily = Roboto,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "150",
                        fontSize = 15.sp,
                        fontFamily = Roboto,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Divider(modifier = Modifier.fillMaxWidth(), color = light_divider)


        }

    }
}

@Composable
fun SendSatsButton() {
    Button(
        modifier = Modifier.width(147.dp).height(40.dp),
        onClick = {},
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colorScheme.tertiary
        ),
        border = BorderStroke(1.dp, light_divider),
    ) {
        Box {
            Text(
                "Send Sats",
                color = Color.Black,
                fontFamily = Roboto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                textAlign = TextAlign.Center
            )
            Image(
                painter = imageResource(Res.drawable.ic_send),
                contentDescription = "",
                modifier = Modifier.align(Alignment.CenterEnd).size(10.dp)
            )
        }
    }
}

