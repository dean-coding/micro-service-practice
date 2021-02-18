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

echo "build and up service container [start]"
if [ -z "${service_name}" ];
    then
        docker-compose -f docker-compose-prod.yml up --force-recreate -d
    else
        docker stop ${service_name}
        docker rm -f ${service_name}
        docker-compose -f docker-compose-prod.yml up --force-recreate -d  --no-deps ${service_name}
fi
echo "build and up service container [over]"

echo "prune none images [start]"
docker image prune -f
echo "prune none images [over]"

sleep 10s
docker-compose -f docker-compose-prod.yml ps