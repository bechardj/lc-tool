package us.jbec.lct.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import us.jbec.lct.models.DocumentSaveEvent;
import us.jbec.lct.services.CaptureDataStatisticsService;

/**
 * Configuration class for evicting cached Service layer results
 */
@EnableScheduling
@EnableCaching
@Configuration
@EnableAsync

public class CachingConfig {

    Logger LOG = LoggerFactory.getLogger(CachingConfig.class);

    private final CaptureDataStatisticsService captureDataStatisticsService;
    private final CacheManager cacheManager;

    public CachingConfig(CaptureDataStatisticsService captureDataStatisticsService, CacheManager cacheManager) {
        this.captureDataStatisticsService = captureDataStatisticsService;
        this.cacheManager = cacheManager;
    }

    /**
     * Clears and regenerates stats cache on document save
     * @param documentSaveEvent document save event
     * @throws JsonProcessingException
     */
    @EventListener
    @Async
    public void regenerateStatsCacheOnSave(DocumentSaveEvent documentSaveEvent) throws JsonProcessingException {
        LOG.debug("Clearing and recalculating statistics cache due to document event");
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
