package swyp.swyp6_team7.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AutoCompleteSuggestion {
    private List<String> suggestions;
}
