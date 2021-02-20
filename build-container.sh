## bash
usage(){
   echo "Usage: `basename $0` [-s <service_name[string]>]"
   exit -1
}

while getopts "s:" opts;
do
    case $opts in
        s) service_name=$OPTARG
        ;;
        *) usage
        ;;
    esac
done
DOCKER_COMPOSE_HOME='/usr/local'

echo "build and up service[${service_name}] container [start]"
 if [ -z "${service_name}" ];
    then
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f docker-compose-prod.yml up --force-recreate -d
    else
        docker stop ${service_name}
        docker rm -f ${service_name}
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f docker-compose-prod.yml up --force-recreate -d  --no-deps ${service_name}
fi
echo "build and up service[${service_name}] container [over]"

echo "prune none images [start]"
docker image prune -f
echo "prune none images [over]"

sleep 10s
${DOCKER_COMPOSE_HOME}/bin/docker-compose -f docker-compose-prod.yml ps