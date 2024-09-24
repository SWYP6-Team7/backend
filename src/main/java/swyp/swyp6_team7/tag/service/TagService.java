package swyp.swyp6_team7.tag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.repository.TagRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag findByName(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.of(name)));
    }

    @Transactional
    public List<Tag> createTags(List<String> tagNames) {
        // 태그 이름 리스트에서 각 태그를 찾거나 생성
        return tagNames.stream()
                .distinct()  // 중복 제거
                .map(this::findByName)  // 각 태그 이름을 통해 태그를 찾거나 생성
                .collect(Collectors.toList());
    }

}
