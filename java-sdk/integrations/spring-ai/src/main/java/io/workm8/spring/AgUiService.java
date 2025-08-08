package io.workm8.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.workm8.agui.client.RunAgentParameters;
import io.workm8.agui.client.subscriber.AgentSubscriber;
import io.workm8.agui.client.subscriber.AgentSubscriberParams;
import io.workm8.agui.event.BaseEvent;
import io.workm8.spring.agent.SpringAgent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public class AgUiService {

    public SseEmitter streamEvents(final SpringAgent agent, final RunAgentParameters parameters) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        var objectMapper = new ObjectMapper();

        agent.runAgent(parameters, new AgentSubscriber() {
            @Override
            public void onEvent(BaseEvent event) {
                try {
                    emitter.send(SseEmitter.event().data(" " + objectMapper.writeValueAsString(event)).build());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public void onRunFinalized(AgentSubscriberParams params) {
                emitter.complete();
            }
            @Override
            public void onRunFailed(AgentSubscriberParams params, Throwable throwable) {
                emitter.completeWithError(throwable);
            }
        });

        return emitter;
    }
}
