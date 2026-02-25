package com.grass.grassaiagent.mapper;

import com.grass.grassaiagent.domain.AiChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Mr.Liuxq
* @description 针对表【ai_chat_session(AI会话表)】的数据库操作Mapper
* @createDate 2026-02-24 16:20:46
* @Entity com.grass.grassaiagent.domain.AiChatSession
*/
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {

}




