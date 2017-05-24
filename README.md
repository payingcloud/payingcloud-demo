# payingcloud-demo

该项目用于演示payingcloud-java-sdk的使用，使用方式如下：

1. 修改配置文件`src/main/resources/application.yml`中的参数
   ```
   api:
     accessKeyId: changeme
     accessKeySecret: changeme      
   weixin: # 微信公众号支付需要
     appId: changeme
     appSecret: changeme
     token: changeme
   ```

1. 启动程序
   ```
   gradlew bootRun
   ```

1. 使用浏览器打开如下地址
   ```
   http://localhost:8080/
   ```
