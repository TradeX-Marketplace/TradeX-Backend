package com.kalibyte.tradex.trading.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * MarketStreamController - Server-Sent Events (SSE) endpoints for real-time
 * commodity trading, price tick streaming, and instant negotiation feedback.
 */
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class MarketStreamController {

    private final MarketStreamService streamService;

    /**
     * GET /api/v1/stream/negotiations/{id}
     * Stream live rounds, counters, and agreement triggers for a negotiation thread
     */
    @GetMapping(value = "/negotiations/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNegotiation(@PathVariable Long id) {
        return streamService.subscribeNegotiation(id);
    }

    /**
     * GET /api/v1/stream/market
     * Stream live market listings and ticker updates
     */
    @GetMapping(value = "/market", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMarket() {
        return streamService.subscribeMarket();
    }
}
