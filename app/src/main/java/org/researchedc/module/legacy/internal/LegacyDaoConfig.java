package org.researchedc.module.legacy.internal;

import org.springframework.context.annotation.Configuration;

/**
 * Declares legacy-core DAOs as Spring beans so they can be injected into
 * legacy-gateway controllers instead of being instantiated with {@code new}.
 *
 * <p>This is a transitional configuration. As each DAO is replaced by a
 * proper Spring Data JPA repository (see Sequence 2 migration), the
 * corresponding bean declaration here can be removed.</p>
 *
 * <p>Current status: ALL 12 original DAOs have been replaced.
 * This class is retained for backward compatibility during cleanup.</p>
 */
@Configuration
public class LegacyDaoConfig {
}
