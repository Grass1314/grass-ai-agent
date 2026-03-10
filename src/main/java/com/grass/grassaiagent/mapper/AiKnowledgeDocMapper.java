package com.grass.grassaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grass.grassaiagent.domain.AiKnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识文档 Mapper — 提供关键词检索能力
 *
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10 10:00
 */
@Mapper
public interface AiKnowledgeDocMapper extends BaseMapper<AiKnowledgeDoc> {

    /**
     * 关键词检索：在标题、内容、关键词字段中模糊匹配
     */
    @Select("SELECT id, title, content, category, keywords " +
            "FROM ai_knowledge_doc " +
            "WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR content LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR keywords LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY id DESC LIMIT #{topK}")
    List<AiKnowledgeDoc> searchByKeyword(@Param("keyword") String keyword, @Param("topK") int topK);
}
