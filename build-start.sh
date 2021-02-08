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


if [ -z "${service_name}" ];
then
    mvn clean package -Dmaven.test.skip=true -P auto-build-images,!snapshots,!public
else
    mvn clean package -Dmaven.test.skip=true -pl ${service_name} -am -P auto-build-images,!snapshots,!public
fi

if [[ $? != "0" ]]
then
    echo "mvn package error"
    exit 1
fi
echo "package and build docker image succeed！！！"

echo "build and up service container [start]"
if [ -z "${service_name}" ];
    then
        docker-compose -f docker-compose-prod.yml up --force-recreate -d
    else
        docker-compose -f docker-compose-prod.yml up --force-recreate -d ${service_name}
fi
echo "container build and starting..."


echo "prune none images [start]"
docker image prune -f
echo "prune none images [over]"

sleep 10s
echo "build and up service container [over]"
docker-compose -f docker-compose-prod.yml ps