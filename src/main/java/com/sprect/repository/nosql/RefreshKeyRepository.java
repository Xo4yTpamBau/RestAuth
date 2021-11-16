package com.sprect.repository.nosql;

import com.sprect.model.redis.RefreshKey;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface RefreshKeyRepository extends KeyValueRepository<RefreshKey, String> {
}
