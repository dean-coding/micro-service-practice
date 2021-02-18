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
    mvn clean package -Dmaven.test.skip=true
else
    mvn clean package -Dmaven.test.skip=true -pl ${service_name} -am
fi

if [[ $? != "0" ]]
then
    echo "mvn package error"
    exit 1
fi
echo "package and compile succeed！！！"