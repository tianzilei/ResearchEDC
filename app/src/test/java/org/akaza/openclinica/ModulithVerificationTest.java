package org.akaza.openclinica;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ModulithVerificationTest {

  /**
   * Verify only the new module packages under {@code org.akaza.openclinica.module}.
   * Legacy packages (bean, dao, domain, etc.) are excluded from verification
   * and will be migrated into modules incrementally.
   */
  static final ApplicationModules MODULES =
      ApplicationModules.of("org.akaza.openclinica.module");

  @Test
  void verifyNewModuleStructure() {
    assertDoesNotThrow(() -> MODULES.verify());
  }
}

