package com.sprect.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "accessKey", timeToLive = 600000)
public class AccessKey {

    @Id
    private String id;

    private String accessKey;
}
