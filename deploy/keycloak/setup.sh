#!/bin/bash
# Keycloak Setup Script for ResearchEDC Development
# Creates the keycloak database in PostgreSQL before starting Keycloak

set -e
echo "Creating Keycloak database if it doesn't exist..."
docker exec researchedc-postgres psql -U researchedc -tc \
  "SELECT 1 FROM pg_database WHERE datname = 'researchedc-keycloak'" | \
  grep -q 1 || docker exec researchedc-postgres psql -U researchedc -c \
  "CREATE DATABASE researchedc-keycloak"

echo "Keycloak database ready."
echo "Start Keycloak: docker compose -f deploy/compose/docker-compose.dev.yml up -d keycloak"
echo "Access: http://localhost:8443"
echo "Admin: admin / admin"
echo "Realm: researchedc"
echo "Test user: admin / admin (researchedc realm)"
