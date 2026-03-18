package com.grass.grassaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grass.grassaiagent.common.R;
import com.grass.grassaiagent.domain.AiKnowledgeDoc;
import com.grass.grassaiagent.service.aiKnowledgeDoc.AiKnowledgeDocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI知识文档管理", description = "AI知识文档的增删改查接口")
@RestController
@RequestMapping("/knowledge/doc")
@RequiredArgsConstructor
public class AiKnowledgeDocController {

    private final AiKnowledgeDocService aiKnowledgeDocService;

    @Operation(summary = "新增知识文档")
    @PostMapping
    public R<AiKnowledgeDoc> create(@RequestBody AiKnowledgeDoc doc) {
        aiKnowledgeDocService.save(doc);
        return R.ok(doc);
    }

    @Operation(summary = "根据ID查询知识文档")
    @GetMapping("/{id}")
    public R<AiKnowledgeDoc> getById(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        return R.ok(aiKnowledgeDocService.getById(id));
    }

    @Operation(summary = "分页查询知识文档列表")
    @GetMapping("/page")
    public R<Page<AiKnowledgeDoc>> page(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "分类（可选筛选）") @RequestParam(required = false) String category,
            @Parameter(description = "关键词搜索（可选）") @RequestParam(required = false) String keyword) {
        Page<AiKnowledgeDoc> page = new Page<>(current, size);
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) {
            wrapper.eq(AiKnowledgeDoc::getCategory, category);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(AiKnowledgeDoc::getTitle, keyword)
                    .or().like(AiKnowledgeDoc::getContent, keyword)
                    .or().like(AiKnowledgeDoc::getKeywords, keyword));
        }
        wrapper.orderByDesc(AiKnowledgeDoc::getCreateTime);
        return R.ok(aiKnowledgeDocService.page(page, wrapper));
    }

    @Operation(summary = "更新知识文档")
    @PutMapping
    public R<Boolean> update(@RequestBody AiKnowledgeDoc doc) {
        return R.ok(aiKnowledgeDocService.updateById(doc));
    }

    @Operation(summary = "删除知识文档")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        return R.ok(aiKnowledgeDocService.removeById(id));
    }
}
