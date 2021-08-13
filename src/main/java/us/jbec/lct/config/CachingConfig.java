package us.jbec.lct.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration class for evicting cached Service layer results
 */
@EnableScheduling
@EnableCaching
@Configuration
public class CachingConfig {

    Logger LOG = LoggerFactory.getLogger(CachingConfig.class);

    /**
     * Periodically reset remote statistics result cache
     */
    @Scheduled(fixedDelayString = "${lct.cache.purge.stats:900000}")
    @CacheEvict(value = "remoteStatistics", allEntries = true)
    public void evictAllRemoteStatistics() {
        LOG.info("Clearing Remote Statistics Cache");
    }

    /**
     * Periodically reset dynamic text cache
     */
    @Scheduled(fixedDelayString = "${lct.cache.purge.dynamictext:300000}")
    @CacheEvict(value = "dynamicText", allEntries = true)
    public void evictAllDynamicText() {
        LOG.info("Clearing Dynamic Text Cache");
    }
}
