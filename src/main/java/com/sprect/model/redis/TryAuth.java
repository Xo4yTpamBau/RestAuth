package com.sprect.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "TryAuth", timeToLive = 600)
public class TryAuth {

    @Id
    private Long id;

    private Long count;
}
