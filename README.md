### Vocai SDK 

#### SDK Integration

+ step1. Add it in your root settings.gradle at the end of repositories:
```groovy
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
+ step2. Add the dependency
```groovy
	dependencies {
            implementation 'com.github.VOC-AI:VOCLiveChat-Android:1.3.7'
	}
```

+ step3. launch chat sdk. `chatId`,`email`,`language` is nullable
```kotlin
    VocaiSDK.getInstance().init(this,false) {
        Log.i("MainActivity","vocai sdk is canceled")
    }

    // 传入userId, 唯一值，建议使用设备ID或者已登录用户的ID（比如shopline1001)
    VocaiSDK.getInstance().startChat("{botId}","{token}", "{chatId}", "{email}", "{language}", "{userId}", hashMapOf(
            "email" to "xxxxxxx@shulex-tech.com"
    ))
    
    // 主动发起消息监听
    VocaiSDK.getInstance().startPollUnread("{botId}", "{userId}")
    
    // 停止监听
    VocaiSDK.getInstance().stopPollUnread()
        
    // 未读消息监听 hasUnread: Boolean
    VocaiSDK.getInstance().subscribeUnread { hasUnread -> 
        LogUtil.info("hasUnread:$hasUnread")
    }
    
    // 从未登录切换到登录态，重新绑定chat的userId
    VocaiSDK.getInstance().bindLoginUser("{true_user_id}")
    
    // 退出登录后，需清除会话，避免数据
    VocaiSDK.getInstance().clearChat()
```
