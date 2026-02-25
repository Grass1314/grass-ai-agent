# AI 会话存储表结构说明

## 设计要点

| 要点           | 说明 |
|--------------|------|
| **会话唯一键**    | `ai_chat_session.conversation_id` 与客户端 `chatId` 一致，便于按会话查询、创建 |
| **消息只认会话主键** | `ai_chat_message.session_id` 仅外键关联 `ai_chat_session.id`，无冗余 conversation_id |
| **查询路径**     | 按 conversationId 查会话 → 用 `session.id` 查/写消息，索引友好 |
| **级联删除**     | 删除会话时通过 FK `ON DELETE CASCADE` 自动删除该会话下所有消息 |
| **防重**       | `(session_id, message_order)` 唯一约束，避免同一会话内序号重复 |

## 使用方式

- **新库**：直接执行 `chat_memory_schema.sql` 建表。