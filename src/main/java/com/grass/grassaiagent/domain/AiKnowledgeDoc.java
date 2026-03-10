package com.grass.grassaiagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI知识文档表（混合检索 MySQL 数据源）
 * ai_knowledge_doc
 *
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10 10:00
 */
@TableName(value = "ai_knowledge_doc")
@Data
public class AiKnowledgeDoc implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "title")
    private String title;

    @TableField(value = "content")
    private String content;

    @TableField(value = "category")
    private String category;

    @TableField(value = "keywords")
    private String keywords;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
