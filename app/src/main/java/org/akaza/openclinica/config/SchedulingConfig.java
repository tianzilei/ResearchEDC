package org.akaza.openclinica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Replaces applicationContext-core-annotation-scheduler.xml.
 *
 * <p>Enables Spring's {@code @Scheduled} annotation support with a thread pool of 42,
 * matching the legacy XML configuration ({@code task:scheduler pool-size="42"}).</p>
 *
 * <p>Existing {@code @Scheduled} methods in {@code BulkEmailSenderService} and
 * {@code JobTriggerService} will continue to work without modification.</p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
