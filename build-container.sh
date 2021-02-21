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

 if [ -z "${service_name}" ];
    then
        echo "build and up service containers [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f docker-compose-prod.yml up
        echo "build and up service containers [start]"
    else
        echo "stop container ${service_name} [start]"
        docker stop ${service_name}
        exitCode=$?

        # 如果导入失败，提醒只发送一封邮件
        if [[ "${exitCode}" -ne "0" ]]; then
           echo "WARN: stop ${service_name} FAILED"
        fi
        echo "stop container ${service_name} [over]"

        echo "remove container ${service_name} [start]"
        docker rm -f ${service_name}
        echo "remove container ${service_name} [over]"

        echo "build and up service[${service_name}] container [start]"
        ${DOCKER_COMPOSE_HOME}/bin/docker-compose -f docker-compose-prod.yml up -d ${service_name}
        echo "build and up service[${service_name}] container [over]"
fi

echo "prune none images [start]"
docker image prune -f
echo "prune none images [over]"

sleep 10s
${DOCKER_COMPOSE_HOME}/bin/docker-compose -f docker-compose-prod.yml ps