# Repository Manager Server
Repository Manager is Open Shop Channel's new generation repository system, designed for simplicity and ease of use.

This repository contains the server used by the Open Shop Channel to serve its official Repository Manager repositories. It is written in Java™ and uses the Spring Boot framework.

## Introducing a Java version of Repository Manager
Repository Manager has been rewritten in Java to take advantage of a more robust programming language and improve performance.

## Usage

### Prerequisites
- JDK 17 or higher
- Maven
- Git

### Setting up
> Please note that we do not offer self-hosting support.

To get the project running locally, you need to follow these steps:

1. Copy `src/main/resources/application.yml` to the working directory and modify it with your values.
2. Use `mvn spring-boot:run` to start the server.
3. After that, you can go to http://localhost:8080 and you should see a setup prompt screen.

## Contributing
Contributions are welcome! Feel free to check open issues and PRs in need of help.

Your best bet is joining our [Discord Server](https://discord.gg/osc) to discuss issues, ideas and pull requests. 
