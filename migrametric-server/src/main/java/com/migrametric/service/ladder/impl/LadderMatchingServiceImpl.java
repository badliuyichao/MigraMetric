package com.migrametric.service.ladder.impl;

import com.migrametric.service.ladder.DataVolumeLadderService;
import com.migrametric.service.ladder.LadderMatchingService;
import com.migrametric.service.ladder.UserCountLadderService;
import com.migrametric.vo.ladder.DataVolumeLadderVO;
import com.migrametric.vo.ladder.LadderMatchResultVO;
import com.migrametric.vo.ladder.UserCountLadderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 阶梯匹配服务实现类
 * <p>
 * 封装数据量和用户数阶梯的统一匹配逻辑，供评估向导调用。
 * </p>
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LadderMatchingServiceImpl implements LadderMatchingService {

    private static final String LADDER_TYPE_DATA_VOLUME = "DATA_VOLUME";
    private static final String LADDER_TYPE_USER_COUNT = "USER_COUNT";

    private final DataVolumeLadderService dataVolumeLadderService;
    private final UserCountLadderService userCountLadderService;

    @Override
    public LadderMatchResultVO matchDataVolume(BigDecimal volume) {
        log.debug("匹配数据量阶梯: volume={}", volume);

        if (volume == null) {
            log.debug("数据量为null，返回null");
            return null;
        }

        DataVolumeLadderVO matchedLadder = dataVolumeLadderService.matchByVolume(volume);
        if (matchedLadder == null) {
            log.debug("未匹配到数据量阶梯: volume={}", volume);
            return null;
        }

        log.debug("匹配到数据量阶梯: name={}, weight={}", matchedLadder.getLadderName(), matchedLadder.getWeight());

        LadderMatchResultVO result = new LadderMatchResultVO();
        result.setLadderId(matchedLadder.getId());
        result.setLadderName(matchedLadder.getLadderName());
        result.setWeight(matchedLadder.getWeight());
        result.setLadderType(LADDER_TYPE_DATA_VOLUME);
        result.setInputValue(volume.toPlainString() + "万条");
        result.setRangeText(matchedLadder.getVolumeRangeText());

        return result;
    }

    @Override
    public LadderMatchResultVO matchUserCount(Integer count) {
        log.debug("匹配用户数阶梯: count={}", count);

        if (count == null) {
            log.debug("用户数为null，返回null");
            return null;
        }

        UserCountLadderVO matchedLadder = userCountLadderService.matchByCount(count);
        if (matchedLadder == null) {
            log.debug("未匹配到用户数阶梯: count={}", count);
            return null;
        }

        log.debug("匹配到用户数阶梯: name={}, weight={}", matchedLadder.getLadderName(), matchedLadder.getWeight());

        LadderMatchResultVO result = new LadderMatchResultVO();
        result.setLadderId(matchedLadder.getId());
        result.setLadderName(matchedLadder.getLadderName());
        result.setWeight(matchedLadder.getWeight());
        result.setLadderType(LADDER_TYPE_USER_COUNT);
        result.setInputValue(count + "人");
        result.setRangeText(matchedLadder.getCountRangeText());

        return result;
    }

    @Override
    public BigDecimal getDataVolumeWeight(BigDecimal volume) {
        DataVolumeLadderVO matchedLadder = dataVolumeLadderService.matchByVolume(volume);
        return matchedLadder != null ? matchedLadder.getWeight() : null;
    }

    @Override
    public BigDecimal getUserCountWeight(Integer count) {
        UserCountLadderVO matchedLadder = userCountLadderService.matchByCount(count);
        return matchedLadder != null ? matchedLadder.getWeight() : null;
    }
}
