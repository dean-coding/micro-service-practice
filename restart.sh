docker-compose -f docker-compose-prod.yml down
docker image prune
echo "prune none docker images！！！"
docker-compose -f docker-compose-prod.yml up -d producer-service
echo "container build and starting..."
sleep 10s
docker-compose -f docker-compose-prod.yml ps