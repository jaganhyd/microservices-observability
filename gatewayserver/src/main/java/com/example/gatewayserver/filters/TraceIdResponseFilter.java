package com.example.gatewayserver.filters;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceIdResponseFilter implements GlobalFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public TraceIdResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        exchange.getResponse().beforeCommit(() -> {

            Span currentSpan = tracer.currentSpan();

            if (currentSpan != null) {
                exchange.getResponse().getHeaders().add(TRACE_ID_HEADER, currentSpan.context().traceId());
            }
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

}

/*
return chain.filter(exchange);
-----------------------------
It passes the request along to the next step in the chain,
eventually routing it to accounts or cards or loans.
So the request goes forward, gets handled,
a response comes back and then it heads out to the client.

exchange.getResponse().beforeCommit(()
---------------------------------------
We want to add the trace ID to the response,
but at the moment our filter first runs,
the response does not exist yet.
The request has not even been sent downstream.
The response gets built later.
        So if you try to add our header right now at the top
of the method, there is nothing to add it to
because there is no response object yet.

Fix:
In the reactive world,committing a response means the response is finalized
and its headers get sent over the wire to the client.
And here is a crucial rule.Once a response is committed, it headers are locked.
You can't add or change a header after that point.It's too late.
So before commit is a hook,it lets us register a piece of code as a callback logic.
So we're saying run this callback logic at the very last moment just before the response is sealed and sent.

So before commit guarantees,
we act at exactly the right instant, response ready,
trace ID is ready, headers not yet logged.*/
