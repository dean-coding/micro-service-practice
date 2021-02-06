## 在项目根目录下运行:

mvn clean package -Dmaven.test.skip=true
if [[ $? != "0" ]] 
then
    echo "mvn打包异常"
    exit 1
fi
echo "package and build docker image succeed！！！"
