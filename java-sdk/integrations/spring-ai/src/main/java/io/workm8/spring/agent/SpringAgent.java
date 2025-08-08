package io.workm8.spring.agent;

import io.workm8.agui.client.AbstractAgent;
import io.workm8.agui.client.stream.IEventStream;
import io.workm8.agui.event.*;
import io.workm8.agui.exception.AGUIException;
import io.workm8.agui.message.BaseMessage;
import io.workm8.agui.input.RunAgentInput;
import io.workm8.agui.state.State;
import io.workm8.spring.message.MessageMapper;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;

import java.time.LocalDateTime;
import java.util.*;

public class SpringAgent extends AbstractAgent {

    private final ChatModel chatModel;

    private final MessageMapper messageMapper;

    public SpringAgent(
        final String agentId,
        final String description,
        final String threadId,
        final List<BaseMessage> messages,
        final ChatModel chatModel,
        final State state,
        final boolean debug
    ) {
        super(agentId, description, threadId, messages, state, debug);

        this.chatModel = chatModel;

        this.messageMapper = new MessageMapper();
    }

    @Override
    protected void run(RunAgentInput input, IEventStream<BaseEvent> stream) {
        var threadId = Objects.nonNull(input.threadId()) ? input.threadId() : UUID.randomUUID().toString();
        var runId = Objects.nonNull(input.runId()) ? input.runId() : UUID.randomUUID().toString();

        // Emit run started event
        stream.next(generateRunStartedEvent(input, runId, threadId));

        var messageId = UUID.randomUUID().toString();
        StringBuilder message = new StringBuilder();

        this.chatModel.stream(
            input.messages()
                .stream()
                .map(this.messageMapper::toSpringMessage)
                .toList()
                .toArray(new Message[0])
        ).doFirst(() -> {
            if (!stream.isCancelled()) {
                var event = new TextMessageStartEvent();
                event.setRole("assistant");
                event.setMessageId(messageId);
                event.setTimestamp(LocalDateTime.now().getNano());
                stream.next(event);
            }
        })
        .doOnNext((res) -> {
            if (!stream.isCancelled() && Objects.nonNull(res) && !res.isEmpty()) {
                var contentEvent = new TextMessageContentEvent();
                contentEvent.setTimestamp(LocalDateTime.now().getNano());
                contentEvent.setDelta(res);
                contentEvent.setMessageId(messageId);
                stream.next(contentEvent);
                message.append(res);
            }
        })
        .doOnError(error -> {
            if (!stream.isCancelled()) {
                stream.error(new AGUIException("Exception on stream", error));
            }
        })
        .doOnCancel(() -> {
            if (!stream.isCancelled()) {
                stream.error(new AGUIException("Stream Cancelled"));
            }
        })
        .doOnComplete(() -> {
            if (!stream.isCancelled()) {
                var textMessageEndEvent = new TextMessageEndEvent();
                textMessageEndEvent.setTimestamp(LocalDateTime.now().getNano());
                textMessageEndEvent.setMessageId(messageId);
                stream.next(textMessageEndEvent);

                // Send messages snapshot event
                var snapshotEvent = new MessagesSnapshotEvent();
                snapshotEvent.setMessages(this.messages);
                snapshotEvent.setTimestamp(LocalDateTime.now().getNano());
                stream.next(snapshotEvent);

                // Send run finished event
                var runFinishedEvent = new RunFinishedEvent();
                runFinishedEvent.setRunId(runId);
                runFinishedEvent.setResult(message.toString());
                runFinishedEvent.setThreadId(threadId);
                runFinishedEvent.setTimestamp(LocalDateTime.now().getNano());
                stream.next(runFinishedEvent);

                // Complete the stream
                stream.complete();
            }
        })
        .subscribe();
    }

    private RunStartedEvent generateRunStartedEvent(final RunAgentInput input, String runId, String threadId) {
        var event = new RunStartedEvent();
        event.setThreadId(threadId);
        event.setRunId(runId);
        event.setTimestamp(LocalDateTime.now().getNano());

        return event;
    }
}