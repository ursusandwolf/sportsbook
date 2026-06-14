package com.example.sportsbook.sports.service;

import com.example.sportsbook.market.api.dto.MarketResponse;
import com.example.sportsbook.market.api.dto.SelectionResponse;
import com.example.sportsbook.market.model.Market;
import com.example.sportsbook.market.repository.MarketRepository;
import com.example.sportsbook.market.repository.SelectionRepository;
import com.example.sportsbook.sports.api.dto.CreateEventRequest;
import com.example.sportsbook.sports.api.dto.EventResponse;
import com.example.sportsbook.sports.model.EventStatus;
import com.example.sportsbook.sports.model.Sport;
import com.example.sportsbook.sports.model.SportEvent;
import com.example.sportsbook.sports.repository.SportEventRepository;
import com.example.sportsbook.sports.repository.SportRepository;
import com.example.sportsbook.market.api.dto.CreateMarketRequest;
import com.example.sportsbook.market.api.dto.CreateSelectionRequest;
import com.example.sportsbook.market.model.Selection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        return eventRepository.findAllWithSportByStatus(EventStatus.SCHEDULED).stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
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

        return mapToEventResponse(eventRepository.save(event));
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

        return mapToMarketResponse(marketRepository.save(market));
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
        return SelectionResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .odds(saved.getOdds())
                .build();
    }

    private EventResponse mapToEventResponse(SportEvent event) {
        List<MarketResponse> markets = marketRepository.findByEventId(event.getId()).stream()
                .map(this::mapToMarketResponse)
                .collect(Collectors.toList());

        return EventResponse.builder()
                .id(event.getId())
                .sportName(event.getSport().getName())
                .name(event.getName())
                .homeParticipant(event.getHomeParticipant())
                .awayParticipant(event.getAwayParticipant())
                .startsAt(event.getStartsAt())
                .status(event.getStatus())
                .markets(markets)
                .build();
    }

    private MarketResponse mapToMarketResponse(Market market) {
        List<SelectionResponse> selections = selectionRepository.findByMarketId(market.getId()).stream()
                .map(s -> SelectionResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .odds(s.getOdds())
                        .build())
                .collect(Collectors.toList());

        return MarketResponse.builder()
                .id(market.getId())
                .marketType(market.getMarketType())
                .status(market.getStatus())
                .selections(selections)
                .build();
    }
}
