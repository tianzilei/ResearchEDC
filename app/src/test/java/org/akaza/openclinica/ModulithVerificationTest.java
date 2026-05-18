package org.akaza.openclinica;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import static org.junit.jupiter.api.Assertions.*;

class ModulithVerificationTest {

  static final ApplicationModules MODULES =
      ApplicationModules.of("org.akaza.openclinica.module");

  @Test
  void verifyNewModuleStructure() {
    assertDoesNotThrow(() -> MODULES.verify());
  }
}
