package com.grass.grassaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grass.grassaiagent.common.R;
import com.grass.grassaiagent.domain.AiChatMessage;
import com.grass.grassaiagent.service.aiChat.AiChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI消息管理", description = "AI聊天消息的增删改查接口")
@RestController
@RequestMapping("/chat/message")
@RequiredArgsConstructor
public class AiChatMessageController {

    private final AiChatMessageService aiChatMessageService;

    @Operation(summary = "新增消息")
    @PostMapping
    public R<AiChatMessage> create(@RequestBody AiChatMessage message) {
        aiChatMessageService.save(message);
        return R.ok(message);
    }

    @Operation(summary = "根据ID查询消息")
    @GetMapping("/{id}")
    public R<AiChatMessage> getById(
            @Parameter(description = "消息ID") @PathVariable String id) {
        return R.ok(aiChatMessageService.getById(id));
    }

    @Operation(summary = "分页查询消息列表")
    @GetMapping("/page")
    public R<Page<AiChatMessage>> page(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "会话ID（可选筛选）") @RequestParam(required = false) String sessionId) {
        Page<AiChatMessage> page = new Page<>(current, size);
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        if (sessionId != null && !sessionId.isBlank()) {
            wrapper.eq(AiChatMessage::getSessionId, sessionId);
        }
        wrapper.orderByAsc(AiChatMessage::getMessageOrder);
        return R.ok(aiChatMessageService.page(page, wrapper));
    }

    @Operation(summary = "更新消息")
    @PutMapping
    public R<Boolean> update(@RequestBody AiChatMessage message) {
        return R.ok(aiChatMessageService.updateById(message));
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(
            @Parameter(description = "消息ID") @PathVariable String id) {
        return R.ok(aiChatMessageService.removeById(id));
    }

    @Operation(summary = "根据会话ID查询全部消息")
    @GetMapping("/list")
    public R<?> listBySessionId(
            @Parameter(description = "会话ID") @RequestParam String sessionId) {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getMessageOrder);
        return R.ok(aiChatMessageService.list(wrapper));
    }
}
