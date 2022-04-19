package chat.sphinx.common.models

import chat.sphinx.wrapper.*
import chat.sphinx.wrapper.chat.Chat
import chat.sphinx.wrapper.chat.getColorKey
import chat.sphinx.wrapper.chat.isConversation
import chat.sphinx.wrapper.chat.isTribeOwnedByAccount
import chat.sphinx.wrapper.contact.Contact
import chat.sphinx.wrapper.contact.getColorKey
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.invite.InviteStatus
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.lightning.asFormattedString
import chat.sphinx.wrapper.message.*
import chat.sphinx.wrapper.message.media.MediaType
import kotlin.jvm.JvmName
import kotlinx.coroutines.flow.Flow
import chat.sphinx.wrapper.invite.Invite as InviteWrapper

/**
 * [DashboardChat]s are separated into 2 categories:
 *  - [Active]: An active chat
 *  - [Inactive]: A contact without a conversation yet, or an Invite
 * */
sealed class DashboardChat {

    abstract val chatName: String?
    abstract val photoUrl: PhotoUrl?
    abstract val sortBy: Long

    abstract val unseenMessageFlow: Flow<Long?>?

    abstract fun getDisplayTime(today00: DateTime): String

    @ExperimentalStdlibApi
    abstract fun getMessageText(): String

    abstract fun hasUnseenMessages(): Boolean

    abstract fun isEncrypted(): Boolean

    abstract fun getColorKey(): String?

    fun getColorKeyFor(contact: Contact?, chat: Chat?): String? {
        return contact?.getColorKey() ?: chat?.getColorKey()
    }

    sealed class Active: DashboardChat() {

        abstract val chat: Chat
        abstract val message: Message?

        open val owner: Contact? = null

        override val sortBy: Long
            get() {
                val lastContentSeenDate = chat.contentSeenAt?.time
                val lastMessageActionDate = message?.date?.time ?: chat.createdAt.time

                if (lastContentSeenDate != null && lastContentSeenDate > lastMessageActionDate) {
                    return lastContentSeenDate
                }
                return lastMessageActionDate
            }

        override fun getDisplayTime(today00: DateTime): String {
            return message?.date?.chatTimeFormat(today00) ?: ""
        }

        fun isMessageSenderSelf(message: Message): Boolean =
            message.sender == chat.contactIds.firstOrNull()

        abstract fun getMessageSender(message: Message, withColon: Boolean = true): String

        fun isMyTribe(owner: Contact?): Boolean =
            chat.isTribeOwnedByAccount(owner?.nodePubKey)

        override fun hasUnseenMessages(): Boolean {
            val ownerId: ContactId? = chat.contactIds.firstOrNull()
            val isLastMessageOutgoing = message?.sender == ownerId
            val lastMessageSeen = message?.seen?.isTrue() ?: true
            val chatSeen = chat.seen.isTrue()
            return !lastMessageSeen && !chatSeen && !isLastMessageOutgoing
        }

        override fun isEncrypted(): Boolean {
            return true
        }

        override fun getColorKey(): String? {
            return getColorKeyFor(null, chat)
        }

        @ExperimentalStdlibApi
        override fun getMessageText(): String {
            val message: Message? = message
            return when {
                message == null -> {
                    ""
                }

                message.status.isDeleted() -> {
                    "Message deleted"
                }
                message.type.isInvoicePayment() -> {
                    val amount: String = message.amount
                        .asFormattedString(separator = ',', appendUnit = true)

                    if (isMessageSenderSelf(message)) {
                        "Payment Sent: $amount"
                    } else {
                        "Payment Received: $amount"
                    }
                }
                message.type.isGroupJoin() -> {
                    "${getMessageSender(message, false)} has joined the tribe"
                }
                message.type.isGroupLeave() -> {
                    "${getMessageSender(message, false)} just left the tribe"
                }
                message.type.isMemberRequest() -> {
                    "${getMessageSender(message, false)} wants to join the tribe"
                }
                message.type.isMemberReject() -> {
                    if (isMyTribe(owner)) {
                        "You have declined the request from ${getMessageSender(message, false)}"
                    } else {
                        "The admin declined your request"
                    }
                }
                message.type.isMemberApprove() -> {
                    if (isMyTribe(owner)) {
                        "You have approved the request from ${getMessageSender(message, false)}"
                    } else {
                        "Welcome! You’re now a member"
                    }
                }
                message.type.isGroupKick() -> {
                    "The admin has removed you from this group"
                }
                message.type.isTribeDelete() -> {
                    "The admin deleted this tribe"
                }
                message.type.isBoost() -> {
                    val amount: String = (message.feedBoost?.amount ?: message.amount)
                        .asFormattedString(separator = ',', appendUnit = true)

                    "${getMessageSender(message, true)} boost ${amount}"
                }
                message.messageDecryptionError -> {
                    "DECRYPTION ERROR..."
                }
                message.type.isMessage() -> {
                    message.messageContentDecrypted?.value?.let { decrypted ->
                        when {
                            message.giphyData != null -> {
                                "${getMessageSender(message)} GIF shared"
                            }
                            message.feedBoost != null -> {
                                val amount: String = (message.feedBoost?.amount ?: message.amount)
                                    .asFormattedString(separator = ',', appendUnit = true)

                                "${getMessageSender(message)} boost ${amount}"
                            }
                            message.podcastClip != null -> {
                                "${getMessageSender(message)}${message.podcastClip?.text ?: ""}"
                            }
                            message.isSphinxCallLink -> {
                                "${getMessageSender(message)}join call"
                            }
                            else -> {
                                "${getMessageSender(message)}$decrypted"
                            }
                        }
                    } ?: "${getMessageSender(message)}..."
                }
                message.type.isInvoice() -> {
                    val amount: String = message.amount
                        .asFormattedString(separator = ',', appendUnit = true)

                    if (isMessageSenderSelf(message)) {
                        "Invoice sent: ${amount}"
                    } else {
                        "Invoice received: ${amount}"
                    }

                }
                message.type.isDirectPayment() -> {
                    val amount: String = message.amount
                        .asFormattedString(separator = ',', appendUnit = true)

                    if (isMessageSenderSelf(message)) {
                        "Payment Sent: $amount"
                    } else {
                        "Payment received: $amount"
                    }
                }
                message.type.isAttachment() -> {
                    message.messageMedia?.let { media ->
                        when (val type = media.mediaType) {
                            is MediaType.Audio -> {
                                "An audio clip"
                            }
                            is MediaType.Image -> {
                                if (type.isGif) {
                                    "a gif"
                                } else {
                                    "an image"
                                }
                            }
                            is MediaType.Pdf -> {
                                "a pdf"
                            }
                            is MediaType.Text -> {
                                "a paid message"
                            }
                            is MediaType.Unknown -> {
                                "an attachment"
                            }
                            is MediaType.Video -> {
                                "a video"
                            }
                            else -> {
                                null
                            }
                        }?.let { element ->
                            val sentString = "sent"

                            "${getMessageSender(message,false)} $sentString $element"
                        }
                    } ?: ""
                }
                else -> {
                    ""
                }
            }
        }

        class Conversation(
            override val chat: Chat,
            override val message: Message?,
            val contact: Contact,
            override val unseenMessageFlow: Flow<Long?>,
        ): Active() {

            init {
                require(chat.type.isConversation()) {
                    """
                    DashboardChat.Conversation is strictly for
                    Contacts. Use DashboardChat.GroupOrTribe.
                """.trimIndent()
                }
            }

            override val chatName: String?
                get() = contact.alias?.value

            override val photoUrl: PhotoUrl?
                get() = chat.photoUrl ?: contact.photoUrl

            override fun getMessageSender(message: Message, withColon: Boolean): String {
                if (isMessageSenderSelf(message)) {
                    return "you" + if (withColon) ": " else ""
                }

                return contact.alias?.let { alias ->
                    alias.value + if (withColon) ": " else ""
                } ?: ""
            }

            override fun getColorKey(): String? {
                return getColorKeyFor(contact, chat)
            }
        }

        class GroupOrTribe(
            override val chat: Chat,
            override val message: Message?,
            override val owner: Contact?,
            override val unseenMessageFlow: Flow<Long?>,
        ): Active() {

            override val chatName: String?
                get() = chat.name?.value

            override val photoUrl: PhotoUrl?
                get() = chat.photoUrl

            override fun getMessageSender(message: Message, withColon: Boolean): String {
                if (isMessageSenderSelf(message)) {
                    return "you" + if (withColon) ": " else ""
                }

                return message.senderAlias?.let { alias ->
                    alias.value + if (withColon) ": " else ""
                } ?: ""
            }

            override fun getColorKey(): String? {
                return getColorKeyFor(null, chat)
            }

        }
    }

    /**
     * Inactive chats are for newly added contacts that are awaiting
     * messages to be sent (the Chat has not been created yet)
     * */
    sealed class Inactive: DashboardChat() {

        override fun getDisplayTime(today00: DateTime): String {
            return ""
        }

        class Conversation(
            val contact: Contact
        ): Inactive() {

            override val chatName: String?
                get() = contact.alias?.value

            override val photoUrl: PhotoUrl?
                get() = contact.photoUrl

            override val sortBy: Long
                get() = contact.createdAt.time

            override val unseenMessageFlow: Flow<Long?>?
                get() = null

            @ExperimentalStdlibApi
            override fun getMessageText(): String {
                return ""
            }

            override fun hasUnseenMessages(): Boolean {
                return false
            }

            override fun isEncrypted(): Boolean {
                return !(contact.rsaPublicKey?.value?.isEmpty() ?: true)
            }

            override fun getColorKey(): String? {
                return getColorKeyFor(contact, null)
            }

        }

        class Invite(
            val contact: Contact,
            val invite: InviteWrapper?
        ): Inactive() {

            override val chatName: String?
                get() =  "Invite: ${contact.alias?.value ?: "Unknown"}"

            override val photoUrl: PhotoUrl?
                get() = contact.photoUrl

            override val sortBy: Long
                get() = Long.MAX_VALUE

            override val unseenMessageFlow: Flow<Long?>?
                get() = null

            @JvmName("getChatName1")
            fun getChatName(): String {
                val contactAlias = contact.alias?.value ?: "unknown"
                return "Invite: $contactAlias"
            }

            @ExperimentalStdlibApi
            override fun getMessageText(): String {

                return when (invite?.status) {
                    is InviteStatus.Pending -> {
                        "Looking for an available node for ${contact.alias?.value ?: "unknown"}"
                    }
                    is InviteStatus.Ready, InviteStatus.Delivered -> {
                        "Ready! Tap to share. Expires in 24 hrs"
                    }
                    is InviteStatus.InProgress -> {
                        "${getChatName()} is signing on"
                    }
                    is InviteStatus.PaymentPending -> {
                        "Tap to pay and activate the invite"
                    }
                    is InviteStatus.ProcessingPayment -> {
                        "Payment sent. Waiting confirmation"
                    }
                    is InviteStatus.Complete -> {
                        "Signup complete"
                    }
                    is InviteStatus.Expired -> {
                        "Expired"
                    }

                    null,
                    is InviteStatus.Unknown -> {
                        ""
                    }
                }
            }

            fun getInviteIconAndColor(): Pair<String, String>? {

                return when (invite?.status) {
                    is InviteStatus.Pending -> {
                        Pair("pending", "orange")
                    }
                    is InviteStatus.Ready, InviteStatus.Delivered -> {
                        Pair("ready", "green")
                    }
                    is InviteStatus.InProgress -> {
                        Pair("in progress", "blue")
                    }
                    is InviteStatus.PaymentPending -> {
                        Pair("payment pending", "white")
                    }
                    is InviteStatus.ProcessingPayment -> {
                        Pair("payment sent", "white")
                    }
                    is InviteStatus.Complete -> {
                        Pair("complete", "green")
                    }
                    is InviteStatus.Expired -> {
                        Pair("expired", "red")
                    }
                    null,
                    is InviteStatus.Unknown -> {
                        null
                    }
                }
            }

            fun getInvitePrice(): Sat? {
                return invite?.price
            }

            override fun hasUnseenMessages(): Boolean {
                return false
            }

            override fun isEncrypted(): Boolean {
                return false
            }

            override fun getColorKey(): String? {
                return getColorKeyFor(contact, null)
            }
        }
    }
}