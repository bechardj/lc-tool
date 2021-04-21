package us.jbec.lct.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@EnableCaching
@Configuration
@Profile("remote")
public class CachingConfig {

    Logger LOG = LoggerFactory.getLogger(CachingConfig.class);

    @Scheduled(fixedDelayString = "${lct.cache.purge.stats:900000}")
    @CacheEvict(value = "remoteStatistics", allEntries = true)
    public void evictAllRemoteStatistics() {
        LOG.info("Clearing Remote Statistics Cache");
    }
}
