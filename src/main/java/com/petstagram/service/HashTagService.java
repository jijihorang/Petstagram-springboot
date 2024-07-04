package com.petstagram.service;

import com.petstagram.dto.HashTagDTO;
import com.petstagram.repository.HashTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HashTagService {
    private final HashTagRepository hashTagRepository;

    public List<HashTagDTO> getAllHashTags() {
        return hashTagRepository.findAll().stream()
                .map(tag -> new HashTagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    public List<Object[]> getHashTagUsageCounts() {
        return hashTagRepository.findHashtagUsageCounts();
    }

    public List<HashTagDTO> getPopularHashTags() {
        List<Object[]> usageCounts = hashTagRepository.findHashtagUsageCounts();

        // 상위 5개 해시태그 선택
        List<HashTagDTO> popularTags = usageCounts.stream()
                .limit(5)
                .map(obj -> new HashTagDTO(null, (String) obj[0]))
                .collect(Collectors.toList());

        return popularTags;
    }
}
