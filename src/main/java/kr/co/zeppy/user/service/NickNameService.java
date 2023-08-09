package kr.co.zeppy.user.service;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.user.entity.NicknameCounter;
import kr.co.zeppy.user.repository.NickNameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NickNameService {
    private static final Integer INIT_FORMAT_SIZE = 4;
    private static final String INIT_ASSIGN_TAG = "#0001";

    private final NickNameRepository nickNameRepository;

    public String getUserTagToNickName(String nickname) {

        try {
            Optional<NicknameCounter> optionalNicknameCounter = nickNameRepository.findByNickname(nickname);
            String userTag;

            if (optionalNicknameCounter.isPresent()) {
                NicknameCounter existingNicknameCounter = optionalNicknameCounter.get();

                if (existingNicknameCounter.hasAvailableNumber()) {
                    Integer availableNumber = existingNicknameCounter.getAndRemoveAvailableNumber();
                    userTag = formatUserTag(nickname, INIT_FORMAT_SIZE, availableNumber);

                } else {
                    int lastAssignedNumber = existingNicknameCounter.getLastAssignedNumber();

                    Integer formatSize = Integer.toString(lastAssignedNumber).length();
                    if (formatSize < INIT_FORMAT_SIZE) {formatSize = INIT_FORMAT_SIZE;}

                    if (formatSize > INIT_FORMAT_SIZE && (Integer.toString(lastAssignedNumber + 1).length() != formatSize)) {
                        userTag = formatUserTag(nickname, formatSize + 1, lastAssignedNumber + 1);

                    } else {
                        userTag = formatUserTag(nickname, formatSize, lastAssignedNumber + 1);
                    }
                    existingNicknameCounter.setLastAssignedNumber(lastAssignedNumber + 1);
                }
            } else {
                NicknameCounter newNicknameCounter = NicknameCounter.builder()
                    .nickname(nickname)
                    .lastAssignedNumber(1)
                    .build();
                nickNameRepository.save(newNicknameCounter);
                userTag = nickname + INIT_ASSIGN_TAG;
            }

            return userTag;

        } catch (Exception e) {
            log.info("오류가 발생했습니다. 임시 UserTag를 생성합니다.");
            String uuidUserTag = UUID.randomUUID().toString();
            NicknameCounter newNicknameCounter = NicknameCounter.builder()
                    .nickname(uuidUserTag)
                    .lastAssignedNumber(1)
                    .build();
            nickNameRepository.save(newNicknameCounter);

            return uuidUserTag + INIT_ASSIGN_TAG;
        }
    }

    private String formatUserTag(String nickname, int formatSize, int number) {
        String format = "%" + "0" + formatSize + "d";
        return nickname + "#" + String.format(format, number);
    }
}
