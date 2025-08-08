package io.workm8.agui.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.workm8.agui.event.BaseEvent;
import io.workm8.agui.json.mixins.EventMixin;
import io.workm8.agui.json.mixins.MessageMixin;
import io.workm8.agui.json.mixins.StateMixin;
import io.workm8.agui.message.BaseMessage;
import io.workm8.agui.state.State;

public class ObjectMapperFactory {

    public static void addMixins(final ObjectMapper objectMapper) {
        objectMapper.addMixIn(BaseMessage.class, MessageMixin.class);
        objectMapper.addMixIn(BaseEvent.class, EventMixin.class);
        objectMapper.addMixIn(State.class, StateMixin.class);
    }
}
