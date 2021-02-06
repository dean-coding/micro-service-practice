## 在项目根目录下运行:

mvn clean package -Dmaven.test.skip=true
if [[ $? != "0" ]] 
then
    echo "mvn打包异常"
    exit 1
fi
echo "package and build docker image succeed！！！"

docker image prune
echo "prune none docker images！！！"

docker-compose -f docker-compose-prod.yml up -d eureka-registry
echo ${c_container_name}"容器正常启动..."
