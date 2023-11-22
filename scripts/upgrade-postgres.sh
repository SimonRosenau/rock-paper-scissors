#!/usr/bin/env bash
set -eo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT_DIRECTORY=$(dirname "$SCRIPT_DIR")

CURRENT_VERSION=$(cat "$ROOT_DIRECTORY/dependencies/postgres/data/PG_VERSION")

# Get new version from docker-compose file
NEW_VERSION=$(grep "image: postgres:" "$ROOT_DIRECTORY/docker-compose.yml" | cut -d: -f3)

echo "Migrating postgres from version $CURRENT_VERSION to $NEW_VERSION"

TIMESTAMP=$(date +%s)
# Create backup directory if not exists
mkdir -p "$ROOT_DIRECTORY/dependencies/postgres/backups"

# Start old postgres server
CONTAINER_ID=$(docker run -d -p 5432:5432 -v "$ROOT_DIRECTORY/dependencies/postgres/data:/var/lib/postgresql/data" -e POSTGRES_PASSWORD=postgres postgres:$CURRENT_VERSION)

# Wait for container to accept connections:
echo "Waiting for postgres to accept connections..."
until docker exec "$CONTAINER_ID" pg_isready -U postgres; do
  sleep 1
done
echo "Postgres is ready!"

# Backup old data
docker exec "$CONTAINER_ID" pg_dumpall -c -U postgres > "$ROOT_DIRECTORY/dependencies/postgres/backups/$TIMESTAMP.sql"
echo "Backup created: $ROOT_DIRECTORY/dependencies/postgres/backups/$TIMESTAMP.sql"

# Stop old postgres server
docker stop "$CONTAINER_ID"
# Remove old postgres server
docker rm "$CONTAINER_ID"

# Create restore directory if not exists
mkdir -p "$ROOT_DIRECTORY/dependencies/postgres/restore"
# Clear restore directory
rm -rf "$ROOT_DIRECTORY/dependencies/postgres/restore/*"

# Start new postgres server
CONTAINER_ID=$(docker run -d -p 5432:5432 -v "$ROOT_DIRECTORY/dependencies/postgres/restore:/var/lib/postgresql/data" -e POSTGRES_PASSWORD=postgres postgres:$NEW_VERSION)

# Wait for container to accept connections:
echo "Waiting for postgres to accept connections..."
until docker exec "$CONTAINER_ID" pg_isready -U postgres; do
  sleep 1
done

# Copy backup file into container:
docker cp "$ROOT_DIRECTORY/dependencies/postgres/backups/$TIMESTAMP.sql" "$CONTAINER_ID:/tmp/backup.sql"

# Restore data
docker exec "$CONTAINER_ID" psql -U postgres -f /tmp/backup.sql

# Allow database inspection
echo "Backup successfully restored. You can now inspect the database at localhost:5432 with user postgres and password postgres."

# Check if user accepts the backup
read -p "Do you want to replace the old database with the new one? [y/N] " yn

# Stop new postgres server
docker stop "$CONTAINER_ID"
# Remove new postgres server
docker rm "$CONTAINER_ID"

# If user accepted the new database, replace the old one
if [[ $yn =~ ^[Yy]$ ]]; then
  # Make backup of old data directory
  mv "$ROOT_DIRECTORY/dependencies/postgres/data" "$ROOT_DIRECTORY/dependencies/postgres/backups/$CURRENT_VERSION"

  # Move new data directory into place
  mv "$ROOT_DIRECTORY/dependencies/postgres/restore" "$ROOT_DIRECTORY/dependencies/postgres/data"
  echo "Database successfully migrated."
else
  # Delete restore directory
  rm -rf "$ROOT_DIRECTORY/dependencies/postgres/restore"
  echo "Database migration aborted."
fi
