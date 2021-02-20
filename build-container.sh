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

echo "build and up service[${service_name}] container [start]"
 if [ -z "${service_name}" ];
    then
       `which docker-compose`  -f docker-compose-prod.yml up --force-recreate -d
    else
       `which docker` stop ${service_name}
       `which docker` rm -f ${service_name}
       `which docker-compose`  -f docker-compose-prod.yml up --force-recreate -d --no-deps ${service_name}
fi
echo "build and up service[${service_name}] container [over]"

echo "prune none images [start]"
`which docker` image prune -f
echo "prune none images [over]"

sleep 10s
`which docker-compose` -f docker-compose-prod.yml ps