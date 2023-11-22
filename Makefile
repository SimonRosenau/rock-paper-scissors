setup:
	chmod +x ./scripts/setup.sh && ./scripts/setup.sh
up:
	docker-compose up -d --remove-orphans
down:
	docker-compose down