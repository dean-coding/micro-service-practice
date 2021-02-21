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
CURRENT_COMPOSE_DIR=`pwd`/docker-compose-prod.yml

 if [ -z "${service_name}" ];
    then
        echo "===> stop containers [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} down
        echo "===> stop containers [over]"

        echo "===> build and up service containers [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} up
        echo "===> build and up service containers [over]"
    else
        echo "===> stop container ${service_name} [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} stop -t 10 ${service_name}
        echo "===> stop container ${service_name} [over]"

        echo "===> remove container ${service_name} [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} rm -f ${service_name}
        echo "===> remove container ${service_name} [over]"

        echo "===> build and up service[${service_name}] container [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} up -d ${service_name}
        echo "===> build and up service[${service_name}] container [over]"
fi

echo "===> prune none images [start]"
docker image prune -f
echo "===> prune none images [over]"
sleep 6s
${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} ps