package com.petstagram.controller;

import com.petstagram.dto.HashTagDTO;
import com.petstagram.entity.HashTagEntity;
import com.petstagram.service.HashTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hashtags")
public class HashTagController {

    @Autowired
    private final HashTagService hashTagService;

    @GetMapping("/list")
    public List<HashTagDTO> getAllHashTags() {
        return hashTagService.getAllHashTags();
    }

    @GetMapping("/usage-counts")
    public ResponseEntity<?> getHashTagUsageCounts() {
        try {
            List<Object[]> usageCounts = hashTagService.getHashTagUsageCounts();
            return ResponseEntity.ok(usageCounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving hashtag usage counts");
        }
    }

    @GetMapping("/popular-hashtags")
    public ResponseEntity<List<HashTagDTO>> getPopularHashTags() {
        List<HashTagDTO> popularHashTags = hashTagService.getPopularHashTags();
        return ResponseEntity.ok(popularHashTags);
    }
}
