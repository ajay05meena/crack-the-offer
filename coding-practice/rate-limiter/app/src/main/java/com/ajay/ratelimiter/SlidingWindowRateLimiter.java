package com.ajay.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SlidingWindowRateLimiter implements RateLimiter{
    private final Map<String, ConcurrentLinkedQueue<Long>> userRequestLogs;
    private final Long maxAllowedRequest;
    private final Long slidingWindowSize;

    public SlidingWindowRateLimiter(Long maxAllowedRequest, Long slidingWindowSize) {
        this.userRequestLogs = new ConcurrentHashMap<>();
        this.maxAllowedRequest = maxAllowedRequest;
        this.slidingWindowSize = slidingWindowSize;
    }


    @Override
    public boolean tryAcquire(String userId) {
        Long currentTimeStamp = System.currentTimeMillis();
        userRequestLogs.putIfAbsent(userId, new ConcurrentLinkedQueue<Long>());
        ConcurrentLinkedQueue<Long> requestQueue = userRequestLogs.get(userId);
        if(!requestQueue.isEmpty() && requestQueue.peek() <= currentTimeStamp - slidingWindowSize){
            requestQueue.poll();
        }
        if(requestQueue.size() < maxAllowedRequest){
            requestQueue.add(currentTimeStamp);
            return true;
        }
        return false;
    }
}
