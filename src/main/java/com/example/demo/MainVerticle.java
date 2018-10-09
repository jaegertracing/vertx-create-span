package com.example.demo;

import io.opentracing.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jaegertracing.Configuration;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

import java.util.Random;

public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    public Tracer orderTracer;
    public Tracer inventoryTracer;

    private static final int WAIT = 100; // ms

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
    }

    @Override
    public void start() {
        logger.info("Agent at: " + System.getenv("JAEGER_AGENT_HOST"));

        System.setProperty("JAEGER_REPORTER_LOG_SPANS", "true");
        System.setProperty("JAEGER_SAMPLER_TYPE", "const");
        System.setProperty("JAEGER_SAMPLER_PARAM", "1");

        orderTracer = Configuration.fromEnv("order").getTracer();
        inventoryTracer = Configuration.fromEnv("inventory").getTracer();

        vertx.createHttpServer().requestHandler(req -> {
            try (Scope scope = orderTracer.buildSpan("requestStarted").startActive(true)) {
                submitOrder(scope.span());
                req.response().putHeader("content-type", "text/plain").end("Hello from Vert.x!");
            }
        }).listen(8080);

        logger.info("HTTP server started on port 8080");
    }

    private void submitOrder(Span parent) {
        try (Scope scope = orderTracer.buildSpan("submitOrder").asChildOf(parent).startActive(true)) {
            scope.span().setTag("order-id", "c85b7644b6b5");
            chargeCreditCard(scope.span());
            doAsync(() -> changeOrderStatus(scope.span()));
            doAsync(() -> dispatchEventToInventory(scope.span()));
        }
    }

    private void chargeCreditCard(Span parent) {
        try (Scope scope = orderTracer.buildSpan("chargeCreditCard").asChildOf(parent).startActive(true)) {
            doWait();
            scope.span().setTag("card", "x123");
            // noop
        }
    }

    private void changeOrderStatus(Span parent) {
        try (Scope ignored = orderTracer.buildSpan("changeOrderStatus").asChildOf(parent).startActive(true)) {
            doWait();
            // noop
        }
    }

    private void dispatchEventToInventory(Span parent) {
        try (Scope scope = orderTracer.buildSpan("dispatchEventToInventory").asChildOf(parent).startActive(true)) {
            doWait();
            doAsync(() -> receiveEvent(scope.span()));
        }
    }

    private void receiveEvent(Span parent) {
        try (Scope scope = inventoryTracer.buildSpan("receiveEvent").asChildOf(parent).startActive(true)) {
            doWait();
            checkInventoryStatus(scope.span());
            updateInventory(scope.span());
            prepareOrderManifest(scope.span());
        }
    }

    private void checkInventoryStatus(Span parent) {
        try (Scope ignored = inventoryTracer.buildSpan("checkInventoryStatus").asChildOf(parent).startActive(true)) {
            doWait();
            // noop
        }
    }

    private void updateInventory(Span parent) {
        try (Scope ignored = inventoryTracer.buildSpan("updateInventory").asChildOf(parent).startActive(true)) {
            doWait();
            // noop
        }
    }

    private void prepareOrderManifest(Span parent) {
        try (Scope ignored = inventoryTracer.buildSpan("prepareOrderManifest").asChildOf(parent).startActive(true)) {
            doWait();
            // noop
        }
    }

    private void doWait() {
        try {
            Thread.sleep(new Random().nextInt(WAIT));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doAsync(Runnable r) {
        new Thread(r).start();
    }

}
