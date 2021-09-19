package us.jbec.lct.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import us.jbec.lct.services.CaptureDataStatisticsService;

/**
 * Configuration class for evicting cached Service layer results
 */
@EnableScheduling
@EnableCaching
@Configuration
public class CachingConfig {

    Logger LOG = LoggerFactory.getLogger(CachingConfig.class);

    private final CaptureDataStatisticsService captureDataStatisticsService;
    private final CacheManager cacheManager;

    public CachingConfig(CaptureDataStatisticsService captureDataStatisticsService, CacheManager cacheManager) {
        this.captureDataStatisticsService = captureDataStatisticsService;
        this.cacheManager = cacheManager;
    }

    /**
     * Periodically reset remote statistics result cache
     */
    @Scheduled(fixedDelayString = "${lct.cache.purge.stats:900000}")
    public void evictAllRemoteStatistics() throws JsonProcessingException {
        LOG.debug("Clearing & Repriming Remote Statistics Cache");
        var cache = cacheManager.getCache("statistics");
        if (cache != null) {
            cache.clear();
        }
        captureDataStatisticsService.calculateAllStatistics();
    }

    /**
     * Periodically reset dynamic text cache
     */
    @Scheduled(fixedDelayString = "${lct.cache.purge.dynamictext:300000}")
    @CacheEvict(value = "dynamicText", allEntries = true)
    public void evictAllDynamicText() {
        LOG.debug("Clearing Dynamic Text Cache");
    }
}
