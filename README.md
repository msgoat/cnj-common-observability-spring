# cnj-common-observability-spring

Provides common components to improve observability of cloud-native applications based on `Spring Boot`.

## Status

![Build status](https://codebuild.eu-west-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiOHEyelYybHhUbnJVeUV5bUxKajFhYndHTm44b0RiUWpoYlByZG9hbnVoQVcrdnF4Z0xYUjl1NFI3K1pWaERBcUpnVGpJUnRQZ0I5YU16UnVIT3RwTTMwPSIsIml2UGFyYW1ldGVyU3BlYyI6InZWYkFsWmdmdEhndG94bW0iLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=main)

## Release information

A changelog can be found in [changelog.md](changelog.md).

## Synopsis

This library provides the following features which improve the observability of any Spring Boot application:

* MDC contextual information for each log entry
* Tracing of inbound / outbound REST requests and outbound / inbound REST responses
* MicroMeter metrics for Undertow

### MDC contextual information

MDC contextual information is switched on by default. 
It can be switched off by setting the configuration property `cnj.observability.logging.mdc.enabled` to `false`.

The following contextual information is added the MDC (if present):

* `userId`: Name or unique identifier of the currently authenticated user
* `traceId`: Open Tracing or Open Telemetry trace ID

### Tracing of REST messages

REST tracing is automatically supported for all kinds of REST endpoints:
* RestControllers via OncePerRequestFilter
* RestTemplate-based REST clients via ClientHttpRequestInterceptor
* WebClient-based REST clients via ExchangeFilterFunction

REST tracing is switched off by default.
It can be switched on by setting the configuration property `cnj.observability.rest.tracing.enabled` to `true`.
All tracing components use a common logger with the same name `group.msg.at.cloud.common.observability.REST_TRACE`.
Each tracing message is logged on INFO level.

Each tracing message starts with an eye-catcher indicating the type of REST request being traced:
* `*** REST REQUEST IN ***` represents an inbound REST request received by the current service
* `*** REST RESPONSE OUT ***` represents an outbound REST request sent to a downstream service
* `*** REST REQUEST OUT ***` represents an outbound REST response sent by the current service
* `*** REST RESPONSE IN ***` represents an inbound REST response received from a downstream service

After the eye-catcher the actual tracing information is written in JSON format.

Depending on the type of REST message, the following data is included in each trace message:

* REST requests: request URI, request method, request headers
* REST responses: request URI of the inbound request which cause the response, HTTP status code, response headers

All confidential headers like `Authorization` are redacted to avoid confidential data in trace messages.

### MicroMeter metrics for Undertow

MicroMeter metrics for Undertow are only activated, if Undertow can be found on the classpath.

If you want to use Undertow instead of Tomcat as the servlet engine of your Spring Boot application, this library
automatically adds a `MeterBinder` to the global MicroMeter registry which exposes extensive Undertow telemetry data
to MicroMeter.

## HOW-TO add observability to your application

Simply add the following dependency to your POM:

```xml 
<dependency>
    <groupId>group.msg.at.cloud.common</groupId>
    <artifactId>cnj-common-observability-spring</artifactId>
    <version>${cnj.common.observability.version}</version>
</dependency>
```

Add the following configuration property to your application properties:

```properties
# observability configuration
cnj.observability.rest.tracing.enabled=true
```

Make sure that logger `group.msg.at.cloud.common.observability.REST_TRACE` is activated on INFO level.