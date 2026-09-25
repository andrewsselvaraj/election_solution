# Election Solution

Spring Boot 4 web application using Thymeleaf for server-side rendered views.

## Requirements

- Java 21+

## Run

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080.

## LangChain sample (LangChain4j + Claude)

`/assistant` is a chat page backed by a [LangChain4j](https://docs.langchain4j.dev) AI service.
The model can call tools in `ElectionTools` (candidates, constituencies, polling schedule)
to answer questions such as "Who is standing in the North constituency?".

```bash
export ANTHROPIC_API_KEY=sk-ant-...
./mvnw spring-boot:run
```

Then open http://localhost:8080/assistant. Without the key the page loads but the assistant is disabled.
Model and token limit are set in `application.properties` (`langchain.anthropic.*`).

## Test

```bash
./mvnw test
```

## Structure

```
src/main/java/com/election/solution
├── ElectionSolutionApplication.java   # entry point
├── ai/
│   ├── ElectionAssistant.java         # LangChain4j AI service interface
│   ├── ElectionTools.java             # @Tool methods the model can call
│   └── LangChainConfig.java           # Anthropic chat model + AiServices wiring
└── controller/
    ├── HomeController.java            # serves "/"
    └── AssistantController.java       # serves "/assistant"
src/main/resources
├── application.properties
├── static/css/styles.css
└── templates/
    ├── layout.html                    # shared head/header/footer fragments
    ├── index.html                     # home page
    └── assistant.html                 # LangChain assistant page
```
