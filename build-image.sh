## bash
## 停止并移除旧的容器 & 构建新的镜像

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

    echo "===> build new images [start]"
    `which mvn` dockerfile:build
    echo "===> build new images [start]"
else
    echo "===> stop container ${service_name} [start]"
    ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} stop -t 10 ${service_name}
    echo "===> stop container ${service_name} [over]"

    echo "===> remove container ${service_name} [start]"
    ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f ${CURRENT_COMPOSE_DIR} rm -f ${service_name}
    echo "===> remove container ${service_name} [over]"

    echo "===> build new image ${service_name} [start]"
    `which mvn` -pl ${service_name} -am dockerfile:build
    echo "===> build new image ${service_name} [over]"
fi

echo "===> prune none images [start]"
docker image prune -f
echo "===> prune none images [over]"
