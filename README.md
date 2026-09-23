# Election Solution

Spring Boot 4 web application using Thymeleaf for server-side rendered views.

## Requirements

- Java 21+

## Run

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080.

## Test

```bash
./mvnw test
```

## Structure

```
src/main/java/com/election/solution
├── ElectionSolutionApplication.java   # entry point
└── controller/HomeController.java     # serves "/"
src/main/resources
├── application.properties
├── static/css/styles.css
└── templates/
    ├── layout.html                    # shared head/header/footer fragments
    └── index.html                     # home page
```
