## 创建Namespace
```
# kubectl create ns jenkins
```

## 创建StorageClass
```
# kubectl apply -f manifest/addons/sc.yaml
```

## 安装
```
# kubectl -n jenkins apply -f manifest/
```

### 设置路由转发
```
# kubectl -n jenkins port-forward --address 192.168.1.129 pods/jenkins-0 8000:8080
```

### 查看密码
```
# kubectl -n jenkins exec jenkins-0 -c jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 解决jenkins下载插件慢的问题

找到jenkins_home卷后装载到本机
```
# kubectl get pv
# kubectl describe pv xxxx
# mount -t glusterfs 192.168.1.110:/vol_b2b1091397bdd616cafe0cf3d421e64e /mnt
```
然后修改配置里的地址
```
# sed -i 's/http:\/\/updates.jenkins-ci.org\/download/https:\/\/mirrors.tuna.tsinghua.edu.cn\/jenkins/g' /mnt/updates/default.json
# sed -i 's/http:\/\/www.google.com/https:\/\/www.baidu.com/g' /mnt/updates/default.json
# sed -i 's/https:\/\/updates.jenkins.io/https:\/\/mirrors.tuna.tsinghua.edu.cn\/jenkins\/updates/g' /mnt/hudson.model.UpdateCenter.xml
```

验证文件是否修改成功
```
# kubectl -n jenkins exec jenkins-0 -c jenkins cat /var/jenkins_home/hudson.model.UpdateCenter.xml
```
重启jenkins
```
# kubectl -n jenkins delete pod jenkins-0
```


