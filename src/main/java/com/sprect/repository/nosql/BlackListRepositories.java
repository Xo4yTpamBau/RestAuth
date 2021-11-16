package com.sprect.repository.nosql;

import com.sprect.model.redis.BlackListTokens;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface BlackListRepositories extends KeyValueRepository<BlackListTokens, String> {
    boolean existsById (String id);
}
