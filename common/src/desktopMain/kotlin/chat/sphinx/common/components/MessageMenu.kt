package chat.sphinx.common.components

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalClipboardManager
import chat.sphinx.common.models.ChatMessage
import chat.sphinx.common.state.EditMessageState
import chat.sphinx.utils.toAnnotatedString
import chat.sphinx.wrapper.message.retrieveTextToShow

@Composable
actual fun MessageMenu(
    chatMessage: ChatMessage,
    editMessageState: EditMessageState,
    isVisible: MutableState<Boolean>
) {
    val dismissKebab = {
        isVisible.value = false
    }
    CursorDropdownMenu(
        expanded = isVisible.value,
        onDismissRequest = dismissKebab
    ) {
        chatMessage.message.retrieveTextToShow()?.let { messageText ->
            if (messageText.isNotEmpty()) {
                val clipboardManager = LocalClipboardManager.current
                DropdownMenuItem(onClick = {
                    clipboardManager.setText(
                        messageText.toAnnotatedString()
                    )
                    dismissKebab()
                }) {
                    Text("copy text")
                }
            }
        }

        DropdownMenuItem(onClick = {
            chatMessage.setAsReplyToMessage(editMessageState)
            dismissKebab()
        }) {
            Text("reply")
        }
        if (chatMessage.isReceived) {
            DropdownMenuItem(onClick = {
                // TODO: Boost is broken...
                chatMessage.boostMessage()
                dismissKebab()
            }) {
                Text("boost")
            }
        }
        if (chatMessage.isSent) {
            DropdownMenuItem(onClick = {
                // TODO: Confirm action...
                chatMessage.deleteMessage()
                dismissKebab()
            }) {
                Text("delete")
            }
        }
    }
}