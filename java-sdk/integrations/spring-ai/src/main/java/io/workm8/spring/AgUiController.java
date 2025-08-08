package io.workm8.spring;

import io.workm8.agui.client.RunAgentParameters;
import io.workm8.agui.state.State;
import io.workm8.spring.agent.SpringAgent;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class AgUiController {

    private final AgUiService agUiService;

    @Autowired
    public AgUiController(
        final AgUiService agUiService
    ) {
        this.agUiService = agUiService;
    }

    @PostMapping(value = "/sse/{agentId}")
    public ResponseEntity<SseEmitter> streamData(@PathVariable("agentId") final String agentId, @RequestBody() final AgUiParameters agUiParameters) {
        var chatModel = OllamaChatModel.builder()
            .defaultOptions(OllamaOptions.builder().model("llama3.2").build())
            .ollamaApi(OllamaApi.builder().baseUrl("http://localhost:11434").build())
            .build();

        SpringAgent agent = new SpringAgent(
            agentId,
            "description",
            Objects.nonNull(agUiParameters.getThreadId()) ? agUiParameters.getThreadId() : UUID.randomUUID().toString(),
            agUiParameters.getMessages().stream().map(m -> {
                if (Objects.isNull(m.getName())) {
                    m.setName("");
                }
                return m;
            }).collect(Collectors.toList()),
            chatModel,
            new State(),
            true
        );

        var parameters = RunAgentParameters.builder()
            .runId(UUID.randomUUID().toString())
            .context(agUiParameters.getContext())
            .forwardedProps(agUiParameters.getForwardedProps())
            .tools(agUiParameters.getTools())
            .build();

        SseEmitter emitter = this.agUiService.streamEvents(agent, parameters);

        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .body(emitter);
    }

}
