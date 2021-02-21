## bash
## 构建并启动的新的容器

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
        echo "===> build and up service containers [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} up
        echo "===> build and up service containers [over]"
    else
        echo "===> build and up service[${service_name}] container [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} up -d ${service_name}
        echo "===> build and up service[${service_name}] container [over]"
fi

sleep 6s
${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} ps