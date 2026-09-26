# Election Solution

Spring Boot 4 web application using Thymeleaf for server-side rendered views.

## Requirements

- Java 21+

## Run

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080.

## LangChain sample (LangChain4j + OpenAI)

`/assistant` is a chat page backed by a [LangChain4j](https://docs.langchain4j.dev) AI service.
The model can call tools in `ElectionTools` (candidates, constituencies, polling schedule, polling booth)
to answer questions such as "Who is standing in the North constituency?".

```bash
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

Then open http://localhost:8080/assistant. Without the key the page loads but the assistant is disabled.

Architecture overview slides: [`docs/LangChain_Election_Architecture.pptx`](docs/LangChain_Election_Architecture.pptx)
(components, request flow, who does what, code map).
Model and token limit are set in `application.properties` (`langchain.openai.*`, default model `gpt-5-mini`).

## Profiles

| Profile | File | Used for |
|---|---|---|
| _none_ (local) | `application.properties` only | `./mvnw spring-boot:run` with no profile |
| `dev` | `application-dev.properties` | Logs OpenAI requests/responses and tool calls |
| `prod` | `application-prod.properties` | Deployment; template cache on, no prompt logging, `PORT`/`OPENAI_MODEL` from env |
| `test` | `application-test.properties` | Automated tests (`@ActiveProfiles("test")`); API key forced empty so OpenAI is never called |

`application.properties` holds the shared settings and is the only file loaded when no
profile is given. Pick a profile with
`SPRING_PROFILES_ACTIVE=prod` or `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`.

The OpenAI key is **never** stored in these files — every profile reads it from the
`OPENAI_API_KEY` environment variable. Alternatively, put it in `config/application.properties`
at the project root (git-ignored; Spring Boot loads it automatically and it overrides the
committed file):

```properties
langchain.openai.api-key=sk-...
```

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
│   └── LangChainConfig.java           # OpenAI chat model + AiServices wiring
└── controller/
    ├── HomeController.java            # serves "/"
    └── AssistantController.java       # serves "/assistant"
src/main/resources
├── application.properties             # shared settings
├── application-{dev,prod,test}.properties
├── static/css/styles.css
└── templates/
    ├── layout.html                    # shared head/header/footer fragments
    ├── index.html                     # home page
    └── assistant.html                 # LangChain assistant page
```
