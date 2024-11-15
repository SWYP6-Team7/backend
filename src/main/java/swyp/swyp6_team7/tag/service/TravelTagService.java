package swyp.swyp6_team7.tag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.TravelTag;
import swyp.swyp6_team7.tag.repository.TravelTagRepository;
import swyp.swyp6_team7.travel.domain.Travel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TravelTagService {

    private final TravelTagRepository travelTagRepository;
    private final TagService tagService;


    @Transactional
    public List<TravelTag> update(Travel travel, List<String> newTags) {

        List<TravelTag> travelTags = travel.getTravelTags();
        Set<String> oldTagNames = travelTags.stream()
                .map(tag -> tag.getTag().getName())
                .collect(Collectors.toSet());

        Set<String> newTagNames = new HashSet<>(newTags);

        // 기존에는 없던 새로운 TravelTag를 추가한다
        for (String tagName : newTagNames) {
            if (!oldTagNames.contains(tagName)) {
                Tag tag = tagService.findByName(tagName);
                TravelTag travelTag = TravelTag.of(travel, tag);
                travel.getTravelTags().add(travelTag);
            }
        }

        // 더이상 필요없는 TravelTag를 삭제한다
        travelTags.removeIf(travelTag -> {
            if (!newTagNames.contains(travelTag.getTag().getName())) {
                travelTagRepository.delete(travelTag);
                return true;
            }
            return false;
        });

        return travelTags;
    }

}
