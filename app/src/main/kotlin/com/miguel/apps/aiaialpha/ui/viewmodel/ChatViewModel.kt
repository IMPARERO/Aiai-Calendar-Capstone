package com.miguel.apps.aiaialpha.ui.viewmodel

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.google.ai.client.generativeai.type.content
import com.miguel.apps.aiaialpha.data.mapper.toChatMessageEntity
import com.miguel.apps.aiaialpha.repo.ChatRepository
import com.miguel.apps.aiaialpha.repo.GeminiAIRepo
import com.miguel.apps.aiaialpha.ui.model.ChatMessage
import com.miguel.apps.aiaialpha.ui.model.Role
import com.miguel.apps.aiaialpha.ui.model.states.ChatUiState
import com.simplemobiletools.calendar.pro.helpers.IcsExporter
import com.simplemobiletools.calendar.pro.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val geminiAIRepo: GeminiAIRepo,
    private val chatRepository: ChatRepository,
    private val context: Context
) : ViewModel() {
    private val generativeModel = geminiAIRepo.getGenerativeModel(
        "gemini-1.5-flash",
        geminiAIRepo.provideConfig()
    )
    private val chat = generativeModel.startChat()

    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState(emptyList()))
    val uiState: StateFlow<ChatUiState> = _uiState

    fun syncAiai() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val events = getEvents() // Implement the actual method to get the events
                val icsExporter = IcsExporter(context)

                icsExporter.exportEventsToString(events, showExportingToast = false) { result, icsContent ->
                    viewModelScope.launch(Dispatchers.IO) {
                        if (result == IcsExporter.ExportResult.EXPORT_OK && icsContent != null) {
                            val userMessage = ChatMessage(
                                text = icsContent,
                                participant = Role.YOU,
                                isPending = true,
                                imageUris = emptyList(),
                                id = "sync_aiai"
                            )

                            chatRepository.insertSingleMessage(userMessage.toChatMessageEntity())
                            _uiState.value = ChatUiState(_uiState.value.messages + userMessage)

                            try {
                                val response = chat.sendMessage(icsContent)
                                response.text?.let { modelResponse ->
                                    val newMessage = ChatMessage(
                                        text = modelResponse,
                                        participant = Role.AIAI,
                                        isPending = false,
                                        imageUris = emptyList(),
                                        id = "sync_aiai"
                                    )
                                    chatRepository.insertSingleMessage(newMessage.toChatMessageEntity())
                                    _uiState.value = ChatUiState(_uiState.value.messages + newMessage)

                                    val updatedUserMessage = userMessage.copy(isPending = false)
                                    chatRepository.insertSingleMessage(updatedUserMessage.toChatMessageEntity())
                                    _uiState.value = ChatUiState(
                                        _uiState.value.messages.map {
                                            if (it.id == userMessage.id) updatedUserMessage else it
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                val errorMessage = ChatMessage(
                                    text = e.localizedMessage.toString(),
                                    participant = Role.ERROR,
                                    isPending = false,
                                    imageUris = emptyList(),
                                    id = "sync_aiai"
                                )
                                chatRepository.insertSingleMessage(errorMessage.toChatMessageEntity())
                                _uiState.value = ChatUiState(_uiState.value.messages + errorMessage)
                            }
                        } else {
                            val failureMessage = ChatMessage(
                                text = "Failed to export events.",
                                participant = Role.ERROR,
                                isPending = false,
                                imageUris = emptyList(),
                                id = "sync_aiai"
                            )
                            chatRepository.insertSingleMessage(failureMessage.toChatMessageEntity())
                            _uiState.value = ChatUiState(_uiState.value.messages + failureMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = e.localizedMessage.toString(),
                    participant = Role.ERROR,
                    isPending = false,
                    imageUris = emptyList(),
                    id = "sync_aiai"
                )
                chatRepository.insertSingleMessage(errorMessage.toChatMessageEntity())
                _uiState.value = ChatUiState(_uiState.value.messages + errorMessage)
            }
        }
    }

    private fun getEvents(): ArrayList<Event> {
        // Implement logic to get the list of events you want to export
        // For now, returning a sample event. Replace with actual implementation.
        return arrayListOf(
            Event(1, 1629262800, 1629266400, "Sample Event") // Sample event in seconds
        )
    }

    fun fetchChats(groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getAllChatMessages(groupId).collect {
                _uiState.value = ChatUiState(it)
            }
        }
    }

    fun deleteChat(groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.deleteChats(groupId)
            _uiState.value = ChatUiState()
        }
    }

    fun sendMessage(
        context: Context,
        userMessage: String,
        groupId: String,
        selectedImages: List<String>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageRequestBuilder = ImageRequest.Builder(context)
            val imageLoader = ImageLoader.Builder(context).build()
            val bitmaps = selectedImages.mapNotNull {
                val imageRequest = imageRequestBuilder
                    .data(it)
                    .size(size = 768)
                    .precision(Precision.EXACT)
                    .build()
                try {
                    val result = imageLoader.execute(imageRequest)
                    if (result is SuccessResult) {
                        return@mapNotNull (result.drawable as BitmapDrawable).bitmap
                    } else {
                        return@mapNotNull null
                    }
                } catch (e: Exception) {
                    return@mapNotNull null
                }
            }

            val record = ChatMessage(
                text = userMessage,
                participant = Role.YOU,
                isPending = true,
                imageUris = selectedImages,
                id = groupId
            )

            chatRepository.insertSingleMessage(record.toChatMessageEntity())
            try {
                if (selectedImages.isEmpty()) {
                    val response = chat.sendMessage(userMessage)
                    response.text?.let { modelResponse ->
                        val newMessage = ChatMessage(
                            text = modelResponse,
                            participant = Role.AIAI,
                            isPending = false,
                            imageUris = emptyList(),
                            id = groupId
                        )
                        chatRepository.insertSingleMessage(newMessage.toChatMessageEntity())
                        _uiState.value = ChatUiState(_uiState.value.messages + newMessage)

                        val updatedUserMessage = record.copy(isPending = false)
                        chatRepository.insertSingleMessage(updatedUserMessage.toChatMessageEntity())
                        _uiState.value = ChatUiState(
                            _uiState.value.messages.map {
                                if (it.id == record.id) updatedUserMessage else it
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = e.localizedMessage.toString(),
                    participant = Role.ERROR,
                    isPending = false,
                    imageUris = emptyList(),
                    id = groupId
                )
                chatRepository.insertSingleMessage(errorMessage.toChatMessageEntity())
                _uiState.value = ChatUiState(_uiState.value.messages + errorMessage)
            }
        }
    }
}
