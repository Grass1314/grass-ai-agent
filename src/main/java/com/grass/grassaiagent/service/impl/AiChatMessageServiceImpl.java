package com.grass.grassaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grass.grassaiagent.domain.AiChatMessage;
import com.grass.grassaiagent.service.AiChatMessageService;
import com.grass.grassaiagent.mapper.AiChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Mr.Liuxq
* @description 针对表【ai_chat_message(AI消息记录表)】的数据库操作Service实现
* @createDate 2026-02-24 16:26:51
*/
@Service
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage>
    implements AiChatMessageService{

}




