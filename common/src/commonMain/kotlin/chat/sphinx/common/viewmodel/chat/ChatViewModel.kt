package chat.sphinx.common.viewmodel.chat

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import chat.sphinx.common.models.ChatMessage
import chat.sphinx.common.state.*
import chat.sphinx.common.viewmodel.chat.payment.PaymentViewModel
import chat.sphinx.concepts.meme_input_stream.MemeInputStreamHandler
import chat.sphinx.concepts.meme_server.MemeServerTokenHandler
import chat.sphinx.concepts.repository.message.model.AttachmentInfo
import chat.sphinx.concepts.repository.message.model.SendMessage
import chat.sphinx.di.container.SphinxContainer
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.response.message
import chat.sphinx.utils.UserColorsHelper
import chat.sphinx.utils.notifications.createSphinxNotificationManager
import chat.sphinx.utils.toAnnotatedString
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.chat.Chat
import chat.sphinx.wrapper.chat.ChatName
import chat.sphinx.wrapper.chat.isTribe
import chat.sphinx.wrapper.contact.Contact
import chat.sphinx.wrapper.contact.getColorKey
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.lightning.toSat
import chat.sphinx.wrapper.message.*
import chat.sphinx.wrapper.message.media.MediaType
import chat.sphinx.wrapper.message.media.MessageMedia
import chat.sphinx.wrapper.message.media.toFileName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okio.Path
import theme.primary_green
import theme.primary_red
import utils.deduceMediaType
import utils.getRandomColorRes
import java.io.IOException
import java.io.InputStream

suspend inline fun MessageMedia.retrieveRemoteMediaInputStream(
    url: String,
    memeServerTokenHandler: MemeServerTokenHandler,
    memeInputStreamHandler: MemeInputStreamHandler
): InputStream? {
    return localFile?.toFile()?.inputStream() ?: host?.let { mediaHost ->
        memeServerTokenHandler.retrieveAuthenticationToken(mediaHost)?.let { authenticationToken ->
            memeInputStreamHandler.retrieveMediaInputStream(
                url,
                authenticationToken,
                mediaKeyDecrypted
            )?.first
        }
    }
}

abstract class ChatViewModel(
    val chatId: ChatId?
) {
    val scope = SphinxContainer.appModule.applicationScope
    val dispatchers = SphinxContainer.appModule.dispatchers
    private val sphinxNotificationManager = createSphinxNotificationManager()
    val messageRepository = SphinxContainer.repositoryModule(sphinxNotificationManager).messageRepository
    val repositoryDashboard = SphinxContainer.repositoryModule(sphinxNotificationManager).repositoryDashboard
    val contactRepository = SphinxContainer.repositoryModule(sphinxNotificationManager).contactRepository
    val chatRepository = SphinxContainer.repositoryModule(sphinxNotificationManager).chatRepository
    private val repositoryMedia = SphinxContainer.repositoryModule(sphinxNotificationManager).repositoryMedia
    val memeServerTokenHandler = SphinxContainer.repositoryModule(sphinxNotificationManager).memeServerTokenHandler
    val memeInputStreamHandler = SphinxContainer.networkModule.memeInputStreamHandler
    private val mediaCacheHandler = SphinxContainer.appModule.mediaCacheHandler

    val networkQueryLightning = SphinxContainer.networkModule.networkQueryLightning

    private val colorsHelper = UserColorsHelper(SphinxContainer.appModule.dispatchers)
    private var messagesLoadJob: Job? = null

    var onNewMessageCallback: (() -> Unit)? = null
    private var messagesSize: Int = 0

    enum class ChatActionsMode {
        MENU, REQUEST, SEND_AMOUNT, SEND_TEMPLATE, SEND_TRIBE
    }

    private val _chatActionsStateFlow: MutableStateFlow<Pair<ChatActionsMode, PaymentViewModel.PaymentData?>?> by lazy {
        MutableStateFlow(null)
    }

    val chatActionsStateFlow: StateFlow<Pair<ChatActionsMode, PaymentViewModel.PaymentData?>?>
        get() = _chatActionsStateFlow.asStateFlow()

    fun toggleChatActionsPopup(
        mode: ChatActionsMode,
        data: PaymentViewModel.PaymentData? = null
    ) {
        if (mode == ChatActionsMode.REQUEST) {
            toast("Request amount not implemented yet")
            return
        }

        _chatActionsStateFlow.value = Pair(mode, data)
    }

    fun hideChatActionsPopup() {
        _chatActionsStateFlow.value = null
    }

    init {
        messagesLoadJob = scope.launch(dispatchers.mainImmediate) {
            loadChatMessages()
        }

        scope.launch(dispatchers.io) {
            readMessages()
        }
    }

    fun cancelMessagesJob() {
        messagesLoadJob?.cancel()
    }

    private suspend fun loadChatMessages() {
        getChat()?.let{ chat ->
            messageRepository.getAllMessagesToShowByChatId(chat.id, 50).firstOrNull()?.let { messages ->
                processChatMessages(chat, messages)
            }

            delay(500L)

            messageRepository.getAllMessagesToShowByChatId(chat.id, 1000).distinctUntilChanged().collect { messages ->
                processChatMessages(chat, messages)
            }
        } ?: run {
            MessageListState.screenState(
                MessageListData.EmptyMessageListData
            )
        }
    }

    private suspend fun processChatMessages(chat: Chat, messages: List<Message>) {
        val owner = getOwner()
        val contact = getContact()

        val tribeAdmin = if (chat.ownerPubKey != null) {
            contactRepository.getContactByPubKey(chat.ownerPubKey!!).firstOrNull()
        } else {
            null
        }

        var contactColorInt:Int? = null

        contact?.let { nnContact ->
            val contactColorKey = nnContact.getColorKey()
            contactColorInt = colorsHelper.getColorIntForKey(
                contactColorKey,
                Integer.toHexString(getRandomColorRes().hashCode())
            )
        }

        val chatMessages = messages.reversed().map { message ->

            val colors = getColorsMapFor(message, contactColorInt, tribeAdmin)

            ChatMessage(
                chat,
                contact,
                message,
                colors,
                accountOwner = { owner },
                boostMessage = {
                    boostMessage(chat, message.uuid)
                },
                flagMessage = {
                    confirm(
                        "Confirm Flagging message",
                        "Are you sure you want to flag this message? This action can not be undone"
                    ) {
                        flagMessage(chat, message)
                    }
                },
                deleteMessage = {
                    confirm(
                        "Confirm Deleting message",
                        "Are you sure you want to delete this message? This action can not be undone"
                    ) {
                        deleteMessage(message)
                    }
                }
            )
        }

        MessageListState.screenState(
            MessageListData.PopulatedMessageListData(
                chatMessages
            )
        )

        if (messagesSize != messages.size) {
            messagesSize = messages.size

            delay(200L)
            onNewMessageCallback?.invoke()
        }
    }

    private suspend fun getColorsMapFor(
        message: Message,
        contactColor: Int?,
        tribeAdmin: Contact?
    ): Map<Long, Int> {
        var colors: MutableMap<Long, Int> = mutableMapOf()

        contactColor?.let {
            colors[message.id.value] = contactColor
        } ?: run {
            val colorKey = message.getColorKey()
            val colorInt = colorsHelper.getColorIntForKey(
                colorKey,
                Integer.toHexString(getRandomColorRes().hashCode())
            )

            colors[message.id.value] = colorInt

            if (message.type.isDirectPayment() && tribeAdmin != null) {
                val recipientColorKey = message.getRecipientColorKey(tribeAdmin.id)
                val recipientColorInt = colorsHelper.getColorIntForKey(
                    recipientColorKey,
                    Integer.toHexString(getRandomColorRes().hashCode())
                )

                colors[-message.id.value] = recipientColorInt
            }
        }

        for (m in  message.reactions ?: listOf()) {
            contactColor?.let {
                colors[m.id.value] = contactColor
            } ?: run {
                val colorKey = m.getColorKey()
                val colorInt = colorsHelper.getColorIntForKey(
                    colorKey,
                    Integer.toHexString(getRandomColorRes().hashCode())
                )

                colors[m.id.value] = colorInt
            }
        }

        message.replyMessage?.let { replyMessage ->
            contactColor?.let {
                colors[replyMessage.id.value] = contactColor
            } ?: run {
                val colorKey = replyMessage.getColorKey()
                val colorInt = colorsHelper.getColorIntForKey(
                    colorKey,
                    Integer.toHexString(getRandomColorRes().hashCode())
                )

                colors[replyMessage.id.value] = colorInt
            }
        }

        return colors
    }

    private fun boostMessage(chat: Chat, messageUUID: MessageUUID?) {
        if (messageUUID == null) return

        scope.launch(dispatchers.mainImmediate) {
            val response = messageRepository.boostMessage(
                chat.id,
                chat.pricePerMessage ?: Sat(0),
                chat.escrowAmount ?: Sat(0),
                messageUUID,
            )

            when (response) {
                is Response.Error -> {
                    toast("Boost payment failed", primary_red)
                }
                is Response.Success -> {}
            }
        }
    }

    private fun flagMessage(chat: Chat, message: Message) {
        scope.launch(dispatchers.mainImmediate) {
            messageRepository.flagMessage(message, chat)
        }
    }

    private fun deleteMessage(message: Message) {
        scope.launch(dispatchers.mainImmediate) {
            when (messageRepository.deleteMessage(message)) {
                is Response.Error -> {
                    toast("Failed to delete Message", primary_red)
                }
                is Response.Success -> {}
            }
        }
    }

    fun readMessages() {
        chatId?.let {
            messageRepository.readMessages(chatId)
        }
    }

    fun getRandomHexCode(): String {
        // TODO: Randomly generate a colour.
        return "#212121"
    }

    private suspend fun getOwner(): Contact {
        return contactRepository.accountOwner.value.let { contact ->
            if (contact != null) {
                contact
            } else {
                var resolvedOwner: Contact? = null
                try {
                    contactRepository.accountOwner.collect { ownerContact ->
                        if (ownerContact != null) {
                            resolvedOwner = ownerContact
                            throw Exception()
                        }
                    }
                } catch (e: Exception) {
                }
                delay(25L)

                resolvedOwner!!
            }
        }
    }

    abstract val chatSharedFlow: SharedFlow<Chat?>

    private suspend fun getChat(): Chat? {
        return chatId?.let { chatRepository.getChatById(it) }
    }

    abstract suspend fun getContact(): Contact?

    protected abstract suspend fun getChatInfo(): Triple<ChatName?, PhotoUrl?, String>?

    abstract val checkRoute: Flow<LoadResponse<Boolean, ResponseError>>

    // Message sending logic...
    abstract var editMessageState: EditMessageState
        protected set

    abstract fun initialState(): EditMessageState

    private inline fun setEditMessageState(update: EditMessageState.() -> EditMessageState) {
        editMessageState = editMessageState.update()
    }

    fun onMessageTextChanged(text: String) {
        editMessageState.messageText.value = text
    }

    fun onPriceTextChanged(text: String) {
        try {
            editMessageState.price.value = text.toLong()
        } catch (e: NumberFormatException) {
            editMessageState.price.value = null
        }
    }

    fun isReplying(): Boolean {
        return editMessageState.replyToMessage.value != null
    }
    fun onMessageFileChanged(filepath: Path) {
        editMessageState.attachmentInfo.value = AttachmentInfo(
            filePath = filepath,
            mediaType = filepath.deduceMediaType(),
            fileName = filepath.name.toFileName(),
            isLocalFile = true
        )
    }

    fun resetMessageFile() {
        editMessageState.attachmentInfo.value = null
    }

    private var sendMessageJob: Job? = null
    fun onSendMessage() {
        if (sendMessageJob?.isActive == true) {
            return
        }

        sendMessageJob = scope.launch(dispatchers.mainImmediate) {
            val sendMessageBuilder = SendMessage.Builder()
                .setChatId(editMessageState.chatId)
                .setContactId(editMessageState.contactId)
                .setText(editMessageState.messageText.value.trim())
                .setPaidMessagePrice(editMessageState.price.value?.toSat())
                .also { builder ->
                    editMessageState.replyToMessage.value?.message?.uuid?.value?.toReplyUUID().let { replyUUID ->
                        builder.setReplyUUID(replyUUID)
                    }
                }

            if (
                editMessageState.price?.value ?: 0 > 0 &&
                editMessageState.messageText.value.isNotEmpty()
            ) {
                //Paid text message
                createPaidMessageFile(editMessageState.messageText.value)?.let { path ->
                    sendMessageBuilder.setAttachmentInfo(
                        AttachmentInfo(
                            filePath = path,
                            mediaType = MediaType.Text,
                            path.name.toFileName(),
                            isLocalFile = true,
                        )
                    )
                }
            }

            editMessageState.attachmentInfo.value?.let { attachmentInfo ->
                sendMessageBuilder.setAttachmentInfo(attachmentInfo)
            }

            val sendMessage = sendMessageBuilder.build()

            if (sendMessage.first != null) {
                sendMessage.first?.let { message ->
                    messageRepository.sendMessage(message)

                    setEditMessageState {
                        initialState()
                    }

                    delay(200L)
                    onNewMessageCallback?.invoke()
                }
            } else if (sendMessage.second != null) {
                toast("Message Validation failed: ${sendMessage.second?.name}", primary_red)
            }
        }
    }

    fun sendCallInvite(audioOnly: Boolean) {
        SphinxCallLink.newCallInvite(null, audioOnly)?.value?.let { newCallLink ->
            editMessageState.messageText.value = newCallLink
            editMessageState.price.value = null

            onSendMessage()
        }
    }

    private var toggleChatMutedJob: Job? = null
    fun toggleChatMuted() {
        if (toggleChatMutedJob?.isActive == true) {
            return
        }
        chatSharedFlow.replayCache.firstOrNull()?.let { chat ->
            toggleChatMutedJob = scope.launch(dispatchers.mainImmediate) {
                Exhaustive@
                when (val response = chatRepository.toggleChatMuted(chat)) {
                    is Response.Error -> {
                        toast(response.cause.message, color = primary_red)
                        delay(2_000)
                    }
                    is Response.Success -> {
                        if (response.value) {
                            toast(
                                message = "Chat is now muted. You won\'t get push notifications\nfor incoming messages on this chat",
                                delay = 3000L
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun createPaidMessageFile(text: String?): Path? {
        if (text.isNullOrEmpty()) {
            return null
        }
        return try {
            val output = mediaCacheHandler.createPaidTextFile("txt")
            mediaCacheHandler.copyTo(text.byteInputStream(), output)
        } catch (e: IOException) {
            null
        }
    }

    private var payAttachmentJob: Job? = null
    fun payAttachment(message: Message) {
        if (payAttachmentJob?.isActive == true) {
            return
        }

        confirm(
            "Confirm Purchase",
            "Are you sure you want to purchase this item?"
        ) {
            payAttachmentJob = scope.launch(dispatchers.mainImmediate) {

                Exhaustive@
                when (val response = messageRepository.payAttachment(message)) {
                    is Response.Error -> {
                        toast(response.cause.message, color = primary_red)
                    }
                    is Response.Success -> {}
                }
            }
        }
    }

    fun downloadFileMedia(message: Message, sent: Boolean) {
        repositoryMedia.downloadMediaIfApplicable(message, sent)
    }

    fun toast(
        message: String,
        color: Color = primary_green,
        delay: Long = 2000L
    ) {
        scope.launch(dispatchers.mainImmediate) {
            sphinxNotificationManager.toast(
                "Sphinx",
                message,
                color.value,
                delay
            )
        }
    }

    private fun confirm(
        title: String,
        message: String,
        callback: () -> Unit
    ) {
        scope.launch(dispatchers.mainImmediate) {
            sphinxNotificationManager.confirmAlert(
                "Sphinx",
                title,
                message,
                callback
            )
        }
    }
}