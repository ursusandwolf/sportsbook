package com.example.sportsbook.sports.service;

import com.example.sportsbook.market.api.dto.CreateMarketRequest;
import com.example.sportsbook.market.api.dto.CreateSelectionRequest;
import com.example.sportsbook.market.api.dto.MarketResponse;
import com.example.sportsbook.market.api.dto.SelectionResponse;
import com.example.sportsbook.market.model.Market;
import com.example.sportsbook.market.model.Selection;
import com.example.sportsbook.market.repository.MarketRepository;
import com.example.sportsbook.market.repository.SelectionRepository;
import com.example.sportsbook.sports.api.dto.CreateEventRequest;
import com.example.sportsbook.sports.api.dto.EventResponse;
import com.example.sportsbook.sports.model.EventStatus;
import com.example.sportsbook.sports.model.Sport;
import com.example.sportsbook.sports.model.SportEvent;
import com.example.sportsbook.sports.repository.SportEventRepository;
import com.example.sportsbook.sports.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SportsService {

    private final SportRepository sportRepository;
    private final SportEventRepository eventRepository;
    private final MarketRepository marketRepository;
    private final SelectionRepository selectionRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> getActiveEvents() {
        List<SportEvent> events = eventRepository.findAllWithSportByStatus(EventStatus.SCHEDULED);
        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream().map(SportEvent::getId).toList();
        List<Market> markets = marketRepository.findByEventIdIn(eventIds);
        Map<Long, List<Market>> marketsByEventId = markets.stream()
                .collect(Collectors.groupingBy(market -> market.getEvent().getId()));

        List<Long> marketIds = markets.stream().map(Market::getId).toList();
        Map<Long, List<Selection>> selectionsByMarketId = marketIds.isEmpty()
                ? Map.of()
                : selectionRepository.findByMarketIdIn(marketIds).stream()
                .collect(Collectors.groupingBy(selection -> selection.getMarket().getId()));

        return events.stream()
                .map(event -> mapToEventResponse(
                        event,
                        marketsByEventId.getOrDefault(event.getId(), List.of()),
                        selectionsByMarketId))
                .toList();
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new IllegalArgumentException("Sport not found"));

        SportEvent event = SportEvent.builder()
                .sport(sport)
                .name(request.name())
                .homeParticipant(request.homeParticipant())
                .awayParticipant(request.awayParticipant())
                .startsAt(request.startsAt())
                .status(EventStatus.SCHEDULED)
                .build();

        SportEvent saved = eventRepository.save(event);
        return mapToEventResponse(saved, List.of(), Map.of());
    }

    @Transactional
    public MarketResponse createMarket(CreateMarketRequest request) {
        SportEvent event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Market market = Market.builder()
                .event(event)
                .marketType(request.marketType())
                .status(request.status())
                .build();

        return mapToMarketResponse(marketRepository.save(market), List.of());
    }

    @Transactional
    public SelectionResponse createSelection(CreateSelectionRequest request) {
        Market market = marketRepository.findById(request.marketId())
                .orElseThrow(() -> new IllegalArgumentException("Market not found"));

        Selection selection = Selection.builder()
                .market(market)
                .name(request.name())
                .odds(request.odds())
                .build();

        Selection saved = selectionRepository.save(selection);
        return toSelectionResponse(saved);
    }

    private EventResponse mapToEventResponse(SportEvent event,
                                             List<Market> markets,
                                             Map<Long, List<Selection>> selectionsByMarketId) {
        List<MarketResponse> marketResponses = markets.stream()
                .map(market -> mapToMarketResponse(
                        market,
                        selectionsByMarketId.getOrDefault(market.getId(), Collections.emptyList())))
                .toList();

        return EventResponse.builder()
                .id(event.getId())
                .sportName(event.getSport().getName())
                .name(event.getName())
                .homeParticipant(event.getHomeParticipant())
                .awayParticipant(event.getAwayParticipant())
                .startsAt(event.getStartsAt())
                .status(event.getStatus())
                .markets(marketResponses)
                .build();
    }

    private MarketResponse mapToMarketResponse(Market market, List<Selection> selections) {
        return MarketResponse.builder()
                .id(market.getId())
                .marketType(market.getMarketType())
                .status(market.getStatus())
                .selections(selections.stream().map(this::toSelectionResponse).toList())
                .build();
    }

    private SelectionResponse toSelectionResponse(Selection selection) {
        return SelectionResponse.builder()
                .id(selection.getId())
                .name(selection.getName())
                .odds(selection.getOdds())
                .build();
    }
}
