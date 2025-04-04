package swyp.swyp6_team7.location.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.location.dto.AutoCompleteSuggestion;
import swyp.swyp6_team7.location.service.LocationAutocompleteService;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/autocomplete")
public class LocationController {
    private final LocationAutocompleteService locationAutocompleteService;

    @GetMapping
    public ApiResponse<AutoCompleteSuggestion> getAutocompleteSuggestions(@RequestParam(value = "location", required = false) String location) {
        if (location == null || location.trim().isEmpty()) {
            return ApiResponse.success(new AutoCompleteSuggestion(Collections.emptyList()));
        }

        List<String> suggestions = locationAutocompleteService.getAutocompleteSuggestions(location);

        return ApiResponse.success(new AutoCompleteSuggestion(suggestions));
    }
}
