package com.miguel.apps.aiaialpha.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

import androidx.compose.ui.unit.dp

import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.ai.sample.util.UriSaver
import com.simplemobiletools.calendar.pro.R
import com.miguel.apps.aiaialpha.data.mapper.toChatMessageEntity
import com.miguel.apps.aiaialpha.data.model.ChatMessageEntity
//dingdingdong
import com.miguel.apps.aiaialpha.ui.component.RoundedTextField
//dingdingdong
import com.miguel.apps.aiaialpha.ui.model.ChatMessage
import com.miguel.apps.aiaialpha.ui.model.Role
import com.miguel.apps.aiaialpha.ui.viewmodel.ChatViewModel
import com.miguel.apps.aiaialpha.util.setClipboard
import com.miguel.apps.aiaialpha.util.shareText
import com.miguel.apps.aiaialpha.util.speakToAdd
import com.miguel.apps.aiaialpha.util.toCamelCase
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import java.io.File
import java.io.FileOutputStream
import java.io.IOException



@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatRoute(
    groupId: String,
    chatViewModel: ChatViewModel = get(),
    onBackPress: (chats: List<ChatMessageEntity>) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatUiState by chatViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var userMessage by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        chatViewModel.fetchChats(groupId)
    }
    BackHandler {
        val list = chatViewModel.uiState.value.messages.map { it.toChatMessageEntity() }
        onBackPress(list)
    }

    val speakLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            userMessage = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
        }
    }

    var openDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                Button(onClick = {
                    chatViewModel.deleteChat(groupId)
                    openDialog = false
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(stringResource(R.string.delete_chat))
            },
            text = { Text(text = stringResource(R.string.confirm_delete)) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aiai") },
                navigationIcon = {
                    IconButton(onClick = {
                        val list = chatViewModel.uiState.value.messages.map { it.toChatMessageEntity() }
                        onBackPress(list)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { openDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Chat")
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Syncing in progress...", Toast.LENGTH_LONG).show()
                        chatViewModel.syncAiai() })
                    {
                        Icon(painter = painterResource(id = R.drawable.sync), contentDescription = "Sync Aiai")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                isResultPending = chatUiState.messages.lastOrNull()?.isPending ?: false && chatUiState.messages.lastOrNull()?.participant == Role.YOU,
                onSendMessage = { inputText, selectedItems ->
                    chatViewModel.sendMessage(
                        context,
                        inputText,
                        groupId = groupId,
                        selectedItems.map { it.toString() }
                    )
                },
                speakLauncher = speakLauncher,
                userMessage = userMessage,
                onValueChange = { userMessage = it },
                resetScroll = {
                    coroutineScope.launch {
                        listState.scrollToItem(0)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ChatList(chatUiState.messages, listState)
        }
    }
}

@Composable
fun ChatList(
    chatMessages: List<ChatMessage>,
    listState: LazyListState,
) {
    LazyColumn(
        reverseLayout = true,
        state = listState
    ) {
        items(chatMessages.reversed()) { message ->
            ChatBubbleItem(message)
        }
    }
}

// Function to generate ICS file content
fun generateICSContent(chatMessage: ChatMessage): String {
    return chatMessage.text.trimIndent()
}

// Function to save ICS file and open it
fun saveAndOpenICSFile(context: Context, icsContent: String) {
    val directory = File(context.getExternalFilesDir(null), "ics")
    if (!directory.exists()) {
        directory.mkdirs()
    }

    val file = File(directory, "event.ics")
    try {
        FileOutputStream(file).use { outputStream ->
            outputStream.write(icsContent.toByteArray())
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val fileUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fileUri, "text/calendar")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}


@Composable
fun ChatBubbleItem(
    chatMessage: ChatMessage,
) {
    val context = LocalContext.current
    val isAIAIMessage =
        chatMessage.participant == Role.AIAI || chatMessage.participant == Role.ERROR
    val backgroundColor = when (chatMessage.participant) {
        Role.AIAI -> MaterialTheme.colorScheme.primaryContainer
        Role.YOU -> MaterialTheme.colorScheme.tertiaryContainer
        Role.ERROR -> MaterialTheme.colorScheme.errorContainer
    }

    val bubbleShape = if (isAIAIMessage) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }

    val horizontalAlignment = if (isAIAIMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }


    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = .0.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = chatMessage.participant.name.toCamelCase(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 0.dp)
        )

        Row {
            BoxWithConstraints {
                Card(
                    shape = bubbleShape,
                    colors = CardDefaults.cardColors(
                        backgroundColor
                    ),
                    modifier = Modifier
                        .padding(0.dp)
                        .wrapContentWidth()
                        .widthIn(0.dp, maxWidth * 0.8f)
                ) {
                    Column {
                        LazyRow(
                            modifier = Modifier.padding(all = .1.dp)
                        ) {
                            items(chatMessage.imageUris) { imageUri ->
                                Box {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .requiredSize(72.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = formatCode(chatMessage.text),
                            modifier = Modifier.padding(10.dp)
                        )
                        Row(
                            Modifier.fillMaxWidth().size(30.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                context.shareText(chatMessage.text)
                            }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = stringResource(R.string.share)
                                )
                            }
                            IconButton(onClick = {
                                context.setClipboard(chatMessage.text)
                            }) {
                                Icon(
                                    painterResource(id = R.drawable.ic_copy),
                                    contentDescription = stringResource(R.string.copy)
                                )
                            }
                            IconButton(onClick = {
                                val icsContent = generateICSContent(chatMessage)
                                saveAndOpenICSFile(context, icsContent)
                            }) {
                                Icon(
                                    painterResource(id = R.drawable.ic_task_vector),
                                    contentDescription = stringResource(R.string.import_to_calendar)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageInput(
    isResultPending: Boolean,
    onSendMessage: (String, List<Uri>) -> Unit = { _, _ -> },
    speakLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    userMessage: String,
    onValueChange: (String) -> Unit,
    resetScroll: () -> Unit = {},
) {
    val context = LocalContext.current
    val imageUris = rememberSaveable(saver = UriSaver()) { mutableStateListOf() }

    val keyboard = LocalSoftwareKeyboardController.current
    var isMessageSent by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = isMessageSent) {
        imageUris.clear()
        isMessageSent = false
    }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column {
            LazyRow(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 12.dp)
            ) {
                items(imageUris) { imageUri ->
                    Box {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .padding(4.dp)
                                .requiredSize(72.dp)
                        )
                        Icon(
                            Icons.Default.Close,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(R.string.remove_image),
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    imageUris.remove(imageUri)
                                }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundedTextField(
                    value = userMessage,
                    placeholder = { Text(stringResource(R.string.chat_label)) },
                    onValueChange = { onValueChange(it) },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // hide keyboard when submit input
                            keyboard?.hide()
                        }
                    ),

                    modifier = Modifier

                        .fillMaxWidth()
                        .weight(0.85f)
                )
                IconButton(
                    onClick = {
                        if (userMessage.isNotBlank()) {
                            onSendMessage(userMessage, imageUris)
                            onValueChange("")
                            resetScroll()
                            isMessageSent = true
                        } else {
                            context.speakToAdd(speakLauncher)
                        }
                    },
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth()
                        .weight(0.15f)
                ) {
                    if (isResultPending) {
                        CircularProgressIndicator()
                    } else {
                        Icon(
                            if (userMessage.isNotBlank()) {
                                painterResource(R.drawable.ic_send)
                            } else {
                                painterResource(R.drawable.ic_voice)
                            },
                            contentDescription = stringResource(R.string.action_send),
                            tint = if (userMessage.isNotBlank())
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (userMessage.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun formatCode(text: String): AnnotatedString {
    val boldRegex = """\*\*(.*?)\*\*""".toRegex()
    val codeRegex = """```([\s\S]*?)```""".toRegex()
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val matches = boldRegex.findAll(text) + codeRegex.findAll(text)
        matches.sortedBy { it.range.first }.forEach { matchResult ->
            // Apply style based on the matched pattern
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1
            if (startIndex > lastIndex) {
                // Append regular text
                append(text.substring(lastIndex, startIndex))
            }
            if (matchResult.groupValues.size >= 2) {
                // Handle bold text
                if (matchResult.value.startsWith("**") && matchResult.value.endsWith("**")) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchResult.groupValues[1])
                    }
                }
                // Handle code block
                else if (matchResult.value.startsWith("```") && matchResult.value.endsWith("```")) {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(matchResult.groupValues[1])
                    }
                }
            }
            lastIndex = endIndex
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
    return annotatedString
}
