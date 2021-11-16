package com.sprect.service.tryAuth;

import com.sprect.exception.TryAuthException;
import com.sprect.model.redis.TryAuth;
import com.sprect.repository.nosql.TryAuthRepository;
import com.sprect.service.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.sprect.utils.DefaultString.BLOCKED_USER_TRY_AUTH;

@Service
@Slf4j
public class TryAuthServiceImpl implements TryAuthService {
    private final TryAuthRepository tryAuthRepository;
    private final MailService mailService;

    public TryAuthServiceImpl(TryAuthRepository tryAuthRepository,
                              MailService mailService) {
        this.tryAuthRepository = tryAuthRepository;
        this.mailService = mailService;
    }

    @Override
    public void checkTryAuth(Long id) {
        Long countTryById = getCountTryById(id);
        saveOrUpdate(id, countTryById);

//        if (countTryById == 5){
//            mailService.sendSuspiciousActivity(userByUEN.getEmail(), "Suspicious activity");
//    }


        log.info("Authorization attempt № {} by the user {}", ++countTryById, id);

        if (countTryById > 10) {
            throw new TryAuthException(BLOCKED_USER_TRY_AUTH);
        }
    }

    private void saveOrUpdate(Long id, Long count) {
        tryAuthRepository.save(new TryAuth(id, ++count));
    }

    private Long getCountTryById(Long id) {
        Optional<TryAuth> byId = tryAuthRepository.findById(id);
        return byId.isPresent() ? byId.get().getCount() : 0L;
    }

    @Override
    public void deleteById(Long id) {
        tryAuthRepository.deleteById(id);
    }


}
