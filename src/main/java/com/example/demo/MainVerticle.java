package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jaegertracing.Configuration;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
    }

    @Override
    public void start() throws Exception {
        String serviceName = System.getenv("JAEGER_SERVICE_NAME");
        if (null == serviceName || serviceName.isEmpty()) {
            serviceName = "vertx-create-span";
        }

        System.setProperty("JAEGER_SERVICE_NAME", serviceName);
        Tracer tracer = Configuration.fromEnv().getTracer();

        vertx.createHttpServer().requestHandler(req -> {
            try (Scope ignored = tracer.buildSpan("operation").startActive(true)) {
                logger.debug("Request received");
                req.response().putHeader("content-type", "text/plain").end("Hello from Vert.x!");
            }
        }).listen(8080);

        logger.info("HTTP server started on port 8080");
    }
}
