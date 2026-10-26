package com.kalibyte.tradex.trading.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MarketStreamService - Real-time SSE streaming for live commodity trading,
 * price negotiations, and market bid ticker.
 */
@Service
@Slf4j
public class MarketStreamService {

    // Emitters by negotiation ID
    private final Map<Long, List<SseEmitter>> negotiationEmitters = new ConcurrentHashMap<>();

    // General market feed emitters
    private final List<SseEmitter> marketEmitters = new CopyOnWriteArrayList<>();

    // Timeout: 30 minutes
    private static final Long EMITTER_TIMEOUT = 30 * 60 * 1000L;

    /**
     * Subscribe to live updates on a specific negotiation thread
     */
    public SseEmitter subscribeNegotiation(Long negotiationId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        List<SseEmitter> emitters = negotiationEmitters.computeIfAbsent(negotiationId, k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(e -> emitters.remove(emitter));

        // Initial connect ping
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Subscribed to negotiation: " + negotiationId));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        log.info("Client subscribed to SSE negotiation stream: {}", negotiationId);
        return emitter;
    }

    /**
     * Subscribe to general market announcements (new listings, rate updates)
     */
    public SseEmitter subscribeMarket() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        marketEmitters.add(emitter);

        emitter.onCompletion(() -> marketEmitters.remove(emitter));
        emitter.onTimeout(() -> {
            marketEmitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(e -> marketEmitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Subscribed to TradeX Market Live Feed"));
        } catch (IOException e) {
            marketEmitters.remove(emitter);
        }

        log.info("Client subscribed to SSE market live feed");
        return emitter;
    }

    /**
     * Broadcast a real-time negotiation update (new round, counter-offer, or agreement)
     */
    public void broadcastNegotiationUpdate(Long negotiationId, String eventType, Object data) {
        List<SseEmitter> emitters = negotiationEmitters.get(negotiationId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    /**
     * Broadcast a market-wide update
     */
    public void broadcastMarketUpdate(String eventType, Object data) {
        if (marketEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : marketEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (Exception e) {
                marketEmitters.remove(emitter);
            }
        }
    }
}
