### 发布流程


```bash
mvn deploy:deploy-file -DgroupId=com.xiaozhuzhu -DartifactId=xzz-repo -Dversion=1.0.1 -Dpackaging=jar -Dfile=xzz-repo/target/xzz-repo-1.0.1.jar -Durl=file://D:/sowftware/info/xiaozhuzhu-repo/xzz-repo -DrepositoryId=xiaozhuzhu-github
````

#### 生成的目录结构

```markdown
<repo-root>/
└─ com/
    └─ xiaozhuzhu/
        └─ xzz-repo/
            └─ 1.0.1/
                ├─ xzz-repo-1.0.1.jar
                └─ xzz-repo-1.0.1.pom

``` 
- jar：你的库文件

- pom：依赖描述文件

- maven-metadata.xml：版本信息
