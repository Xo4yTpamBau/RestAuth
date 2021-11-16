package com.sprect.repository.nosql;

import com.sprect.model.redis.TryAuth;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface TryAuthRepository extends KeyValueRepository<TryAuth, Long> {
}
