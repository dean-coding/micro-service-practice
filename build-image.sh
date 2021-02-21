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
    mvn dockerfile:build
else
    mvn -pl ${service_name} -am dockerfile:build
fi

if [[ $? != "0" ]]
then
    echo "build docker image error"
    exit 1
fi
echo "build docker image succeed！！！"