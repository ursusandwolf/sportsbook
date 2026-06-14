package com.example.sportsbook.sports.api;

import com.example.sportsbook.sports.api.dto.EventResponse;
import com.example.sportsbook.sports.service.SportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportsController {

    private final SportsService sportsService;

    @GetMapping("/events")
    public List<EventResponse> getEvents() {
        return sportsService.getActiveEvents();
    }
}
