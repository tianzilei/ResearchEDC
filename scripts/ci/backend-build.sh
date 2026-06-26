#!/usr/bin/env bash
set -euo pipefail

require_jdk21() {
  if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: java is not on PATH. Set JAVA_HOME to a JDK 21 installation and prepend \$JAVA_HOME/bin." >&2
    exit 1
  fi
  if ! command -v javac >/dev/null 2>&1; then
    echo "ERROR: javac is not on PATH. Set JAVA_HOME to a JDK 21 installation and prepend \$JAVA_HOME/bin." >&2
    exit 1
  fi

  local javac_version
  javac_version="$(javac -version 2>&1)"
  case "${javac_version}" in
    *" 21"*|*" 21."*) ;;
    *)
      echo "ERROR: javac 21 is required; found: ${javac_version}" >&2
      exit 1
      ;;
  esac

  echo "Using $(java -version 2>&1 | head -n 1)"
  echo "Using ${javac_version}"
}

require_jdk21
echo "=== Backend: Maven compile ==="
mvn -B clean compile -DskipTests
echo "=== Backend: Modulith verification ==="
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
echo "=== Backend: Module tests ==="
mvn test -pl app -am
echo "=== Backend: OK ==="
