package io.workm8.spring.message;

import org.springframework.ai.chat.messages.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class MessageMapper {

    public org.springframework.ai.chat.messages.AssistantMessage toSpringMessage(final io.workm8.agui.message.AssistantMessage message) {
        return new org.springframework.ai.chat.messages.AssistantMessage(
            message.getContent(),
            Map.of(
                "id",
                Objects.nonNull(message.getId()) ? message.getId() : UUID.randomUUID().toString(),
                "name",
                Objects.nonNull(message.getName()) ? message.getName() : ""
            ),
            Objects.isNull(message.getToolCalls())
                ? emptyList()
                : message.getToolCalls().stream().map(toolCall ->
                    new AssistantMessage.ToolCall(
                        Objects.nonNull(toolCall.id())
                            ? toolCall.id()
                            : UUID.randomUUID().toString(),
                        toolCall.type(),
                        toolCall.function().name(),
                        toolCall.function().arguments()
                    )
                ).collect(Collectors.toList())
        );
    }

    public org.springframework.ai.chat.messages.SystemMessage toSpringMessage(final io.workm8.agui.message.SystemMessage message) {
        return SystemMessage.builder()
            .text(message.getContent())
            .metadata(
                Map.of(
                    "id",
                    Objects.nonNull(message.getId()) ? message.getId() : UUID.randomUUID().toString(),
                    "name",
                    Objects.nonNull(message.getName()) ? message.getName() : ""
                )
            ).build();
    }

    public org.springframework.ai.chat.messages.UserMessage toSpringMessage(final io.workm8.agui.message.DeveloperMessage message) {
        return UserMessage.builder()
            .text(message.getContent())
            .metadata(
                Map.of(
                    "id",
                    Objects.nonNull(message.getId()) ? message.getId() : UUID.randomUUID().toString(),
                    "name",
                    Objects.nonNull(message.getName()) ? message.getName() : ""
                )
            ).build();
    }

    public org.springframework.ai.chat.messages.ToolResponseMessage toSpringMessage(final io.workm8.agui.message.ToolMessage message) {
        return new ToolResponseMessage(
            asList(
                new ToolResponseMessage.ToolResponse(
                    message.getToolCallId(),
                    message.getName(),
                    Objects.nonNull(message.getError())
                        ? message.getError()
                        : message.getContent()
                )
            ),
            Map.of(
                "id",
                Objects.nonNull(message.getId()) ? message.getId() : UUID.randomUUID().toString(),
                "name",
                Objects.nonNull(message.getName()) ? message.getName() : ""
            )
        );
    }

    public org.springframework.ai.chat.messages.UserMessage toSpringMessage(final io.workm8.agui.message.UserMessage message) {
        return UserMessage.builder()
            .text(message.getContent())
            .metadata(
                Map.of(
                    "id",
                    Objects.nonNull(message.getId()) ? message.getId() : UUID.randomUUID().toString(),
                    "name",
                    Objects.nonNull(message.getName()) ? message.getName() : ""
                )
            ).build();
    }

    public org.springframework.ai.chat.messages.AbstractMessage toSpringMessage(final io.workm8.agui.message.BaseMessage baseMessage) {
        switch (baseMessage.getRole()) {
            case "assistant" -> {
                return this.toSpringMessage((io.workm8.agui.message.AssistantMessage) baseMessage);
            }
            case "system" -> {
                return this.toSpringMessage((io.workm8.agui.message.SystemMessage) baseMessage);
            }
            case "developer" -> {
                return this.toSpringMessage((io.workm8.agui.message.DeveloperMessage) baseMessage);
            }
            case "tool" -> {
                return this.toSpringMessage((io.workm8.agui.message.ToolMessage) baseMessage);
            }
            default -> {
                return this.toSpringMessage((io.workm8.agui.message.UserMessage) baseMessage);
            }
        }
    }
}