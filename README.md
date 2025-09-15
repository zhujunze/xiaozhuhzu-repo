### 发布流程


```bash
mvn deploy:deploy-file -DgroupId=com.xiaozhuzhu -DartifactId=xzz-repo -Dversion=1.0.0 -Dpackaging=jar -Dfile=xzz-repo/target/xzz-repo-1.0.0.jar -Durl=file:./xzz-repo -DrepositoryId=xiaozhuzhu-github
````

#### 生成的目录结构

```markdown
xiaozhuzhu-repo/
└── com/
    └── xiaozhuzhu/
        └── xiaozhuzhu-repo/
            └── 1.0.0/
                ├── xiaozhuzhu-repo-1.0.0.jar
                ├── xiaozhuzhu-repo-1.0.0.pom
                └── maven-metadata.xml
``` 
- jar：你的库文件

- pom：依赖描述文件

- maven-metadata.xml：版本信息
