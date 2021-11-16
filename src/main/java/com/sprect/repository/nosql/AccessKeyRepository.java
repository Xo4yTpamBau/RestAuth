package com.sprect.repository.nosql;

import com.sprect.model.redis.AccessKey;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface AccessKeyRepository extends KeyValueRepository<AccessKey, String> {
}
