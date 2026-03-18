package com.grass.grassaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grass.grassaiagent.common.R;
import com.grass.grassaiagent.domain.AiChatSession;
import com.grass.grassaiagent.service.aiChat.AiChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI会话管理", description = "AI会话的增删改查接口")
@RestController
@RequestMapping("/chat/session")
@RequiredArgsConstructor
public class AiChatSessionController {

    private final AiChatSessionService aiChatSessionService;

    @Operation(summary = "新增会话")
    @PostMapping
    public R<AiChatSession> create(@RequestBody AiChatSession session) {
        aiChatSessionService.save(session);
        return R.ok(session);
    }

    @Operation(summary = "根据ID查询会话")
    @GetMapping("/{id}")
    public R<AiChatSession> getById(
            @Parameter(description = "会话ID") @PathVariable String id) {
        return R.ok(aiChatSessionService.getById(id));
    }

    @Operation(summary = "分页查询会话列表")
    @GetMapping("/page")
    public R<Page<AiChatSession>> page(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "用户ID（可选筛选）") @RequestParam(required = false) String userId) {
        Page<AiChatSession> page = new Page<>(current, size);
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && !userId.isBlank()) {
            wrapper.eq(AiChatSession::getUserId, userId);
        }
        wrapper.orderByDesc(AiChatSession::getCreateTime);
        return R.ok(aiChatSessionService.page(page, wrapper));
    }

    @Operation(summary = "更新会话")
    @PutMapping
    public R<Boolean> update(@RequestBody AiChatSession session) {
        return R.ok(aiChatSessionService.updateById(session));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(
            @Parameter(description = "会话ID") @PathVariable String id) {
        return R.ok(aiChatSessionService.removeById(id));
    }
}
