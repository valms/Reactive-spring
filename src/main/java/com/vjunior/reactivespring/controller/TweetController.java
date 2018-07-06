package com.vjunior.reactivespring.controller;


import com.vjunior.reactivespring.model.Tweet;
import com.vjunior.reactivespring.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class TweetController {

    private final TweetRepository tweetRepository;

    @Autowired
    public TweetController(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    @GetMapping("/tweets")
    public Flux<Tweet> getAllTweets() {
        return this.tweetRepository.findAll();
    }

    @PostMapping("/tweets")
    public Mono<Tweet> createTweet(@Valid @RequestBody Tweet tweet) {
        return this.tweetRepository.save(tweet);
    }

    @GetMapping("/tweet/{id}")
    public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable(value = "id") String id) {
        return this.tweetRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/tweet/{id}")
    public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable(value = "id") String tweetId,
                                                   @Valid @RequestBody Tweet tweet) {

        return this.tweetRepository.findById(tweetId).flatMap(actualTweet -> {
            actualTweet.setText(tweet.getText());
            return this.tweetRepository.save(actualTweet);
        })
                .map(updatedTweet -> new ResponseEntity<>(updatedTweet, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/tweet/{id}")
    public Mono<ResponseEntity<Void>> deleteTweet(@PathVariable(value = "id") String tweetId) {

        return this.tweetRepository.findById(tweetId)
                .flatMap(existingTweet -> this.tweetRepository.delete(existingTweet)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweets() {
        return tweetRepository.findAll();
    }
}