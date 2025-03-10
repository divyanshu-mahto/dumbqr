package com.dumbqr.dumbqr.service;

import org.apache.commons.codec.digest.MurmurHash3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BloomFilterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String BLOOM_FILTER_KEY = "bloom-filter";
    private static final int BIT_SIZE = 9585058; //for 1 million records
    private static final int[] HASH_SEEDS = {0,12,24,42,67,82,91}; //7 hash functions

    public void add(String st){
        byte[] data = st.getBytes();

        for(int seed : HASH_SEEDS){
            int hash = MurmurHash3.hash32x86(data,0,data.length, seed);
            int pos = Math.abs(hash) % BIT_SIZE;
            redisTemplate.opsForValue().setBit(BLOOM_FILTER_KEY,pos,true);
        }
    }

    public boolean lookUp(String st){
        byte[] data = st.getBytes();

        for(int seed : HASH_SEEDS){
            int hash = MurmurHash3.hash32x86(data, 0, data.length, seed);
            int pos = Math.abs(hash) % BIT_SIZE;
            if(Boolean.FALSE.equals(redisTemplate.opsForValue().getBit(BLOOM_FILTER_KEY, pos))){
                return false;
            }
        }
        return true;
    }
}
