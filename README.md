# INBOXmove

## Description

INBOXmove is a simple application based on Spring Boot and Spring Integration to move incoming messages from one IMAP
account to another IMAP account. On the inbound side it uses IMAP IDLE to receive events of incoming messages, on the
outbound side a new IMAP connection to the outbound destination is created to append a new message to an IMAP folder.

## Configuration

The configuration approach is bases on Spring Boot's mechanisms. You could use a configuration based on environment
variables, a properties file or a YAML file. I prefer to mount a YAML file into the running container and point Spring
Boot to resolve the configuration file.

An example of a [YAML configuration file](example-configuration.yaml) can be found in this repository.
Use `SPRING_CONFIG_IMPORT=file:/configuration.yaml` to inform Spring Boot where it can find your configuration file.

## Usage (docker-compose)

A simple example using docker-compose (or podman-compose) to run the container with external configuration file mounted
into the container:

```
version: "3"
services:
  inboxmove:
    image: ghcr.io/f466162/inboxmove:main
    restart: unless-stopped
    environment:
      - SPRING_CONFIG_IMPORT=file:/configuration.yaml
    volumes:
      - /path/to/configuration.yaml:/configuration.yaml
```

## License

See [LICENSE](LICENSE) for details.
