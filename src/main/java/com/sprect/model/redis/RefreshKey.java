package com.sprect.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "refreshKey", timeToLive = 86400000)
public class RefreshKey {

    @Id
    private String id;

    private String refreshKey;
}
