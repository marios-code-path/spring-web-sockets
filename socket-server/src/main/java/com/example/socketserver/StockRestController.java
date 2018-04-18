package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class StockRestController {

    private final StockService stockService;

    public StockRestController(StockService stockService) {
        this.stockService = stockService;
    }

    @PutMapping("/subscribe/{ticker}")
    void subscribeTo(@RequestHeader("client-id") String clientId,
                     @PathVariable("ticker") String ticker) {
        stockService.clientSubscribeTo(clientId, ticker);
    }

}
