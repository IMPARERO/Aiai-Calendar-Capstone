package com.miguel.apps.aiaialpha.repo

import com.miguel.apps.aiaialpha.data.model.ChatMessageEntity
import com.miguel.apps.aiaialpha.data.model.GroupEntity
import com.miguel.apps.aiaialpha.ui.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getAllGroups(): Flow<List<GroupEntity>>

    suspend fun insertChatsInGroup(groupEntity: GroupEntity)

    suspend fun updateChatsInGroup(groupEntity: GroupEntity)

    suspend fun getById(groupId: Int): GroupEntity

    fun getAllChatMessages(id: String): Flow<List<ChatMessage>>

    suspend fun insertSingleMessage(chatMessageEntity: ChatMessageEntity)

    suspend fun deleteChats(groupId: String)
}