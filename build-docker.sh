## 在项目根目录下运行:

mvn clean package -Dmaven.test.skip=true
if [[ $? != "0" ]] 
then
    echo "mvn打包异常"
    exit 1
fi
echo "package and build docker image succeed！！！"
##
cd ./mygirl-c-app
mvn dockerfile:build

#image_name="himygirl/mygirl-c-app"
#app_version="2.0.5"
#docker tag ${image_name} ${image_name}:${app_version}

c_container_name="hgCapp"
docker rm -f ${c_container_name}
docker-compose -f docker-compose-prod.yml up -d eureka-registry
echo ${c_container_name}"容器正常启动..."


cd ../mygirl-b-app
mvn dockerfile:build
b_container_name="hgBapp"
docker rm -f ${b_container_name}
docker-compose -f docker-compose-prod.yml up -d ${b_container_name}
echo ${b_container_name}"容器正常启动..."
