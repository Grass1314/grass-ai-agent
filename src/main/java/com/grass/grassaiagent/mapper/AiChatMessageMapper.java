package com.grass.grassaiagent.mapper;

import com.grass.grassaiagent.domain.AiChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Mr.Liuxq
* @description 针对表【ai_chat_message(AI消息记录表)】的数据库操作Mapper
* @createDate 2026-02-24 16:26:51
* @Entity com.grass.grassaiagent.domain.AiChatMessage
*/
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {

}




