package com.example.sportsbook.sports.api;

import com.example.sportsbook.market.api.dto.CreateMarketRequest;
import com.example.sportsbook.market.api.dto.CreateSelectionRequest;
import com.example.sportsbook.market.api.dto.MarketResponse;
import com.example.sportsbook.market.api.dto.SelectionResponse;
import com.example.sportsbook.sports.api.dto.CreateEventRequest;
import com.example.sportsbook.sports.api.dto.EventResponse;
import com.example.sportsbook.sports.service.SportsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
public class AdminSportsController {

    private final SportsService sportsService;

    @PostMapping("/events")
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        return sportsService.createEvent(request);
    }

    @PostMapping("/markets")
    public MarketResponse createMarket(@Valid @RequestBody CreateMarketRequest request) {
        return sportsService.createMarket(request);
    }

    @PostMapping("/selections")
    public SelectionResponse createSelection(@Valid @RequestBody CreateSelectionRequest request) {
        return sportsService.createSelection(request);
    }
}
