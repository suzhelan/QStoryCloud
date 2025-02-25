# QStoryCloud 自动云更新

# 目前项目处于无法使用阶段 请等待一段时间 我正在安排时间维护此项目 (也许半年或者一年内)

### 项目介绍
该项目主要用于快捷的为QStory进行内置自动更新，减少用户手动更新模块的麻烦  
注：此项目不包含QStory本体的源码
---
### 功能
自动检测QStory的更新，并且自动更新，加载到QQ
---
### 实现原理
通过检测在线版本和本地版本是否匹配，不匹配则拉取在线版本的APK  
再通过Dex/Apk热加载进行加载模块
---
### 使用的技术栈
- ~~SQLite~~ ~~改为使用MMKV~~,使用FastKv，简单数据用SQLite会增加数据库维护成本
- XPosed Hook
- OkHttp,~~RxJava~~ Kotlin协程Flow更加轻量,FastJSON
- 跨进程通信（跨应用）ContentProvider
- 热更新（基于DexClassLoader)
- 设计模式:观察者，异步回调等
---
### 项目主要知识要点
 - 如何动态加载模块并进行Hook：[ModuleLoader](./app/src/main/java/top/linl/qstorycloud/hook/moduleloader/ModuleLoader.java)  
 - 如何进行模块的更新,主要通过观察者模式实现定时拉取更新：[update](./app/src/main/java/top/linl/qstorycloud/hook/update) ,[更新检测](.app/src/main/java/top/linl/qstorycloud/hook/update/UpdateObserver.kt),[观察和处理检测结果](./app/src/main/java/top/linl/qstorycloud/hook/update/UpdateChecker.kt)
 - 处理下载的任务和通知 [DownloadTask](./app/src/main/java/top/linl/qstorycloud/hook/update/util/DownloadTask.java)
 - 数据存储配置MMKV相关 [Config](.app/src/main/java/top/linl/qstorycloud/config)
 - 如何使模块和QQ进行跨进程通讯采用的是[ContentProvider](./app/src/main/java/top/linl/qstorycloud/provider/AppContentProvider.java)
---


