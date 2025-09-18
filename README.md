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
            implementation 'com.github.VOC-AI:VOCLiveChat-Android:1.3.9'
	}
```

+ step3. Launch chat sdk. `chatId`,`email`,`language`,`userId` is nullable
```kotlin
    VocaiSDK.getInstance().init(this, isDebugMode = false) {
        Log.i("MainActivity", "vocai sdk is init")
    }

    // 传入userId, 唯一值，建议使用设备ID或者已登录用户的ID（比如shopline1001)
    // back_btn = true, chat页面展示返回按钮
    VocaiSDK.getInstance().startChat("{botId}","{token}", "{chatId}", "{email}", "{language}", "{userId}", hashMapOf(
            "email" to "xxxxxxx@shulex-tech.com", "noBrand" to "true", "back_btn" to "true"
    ))

    // example 
    VocaiSDK.getInstance().startChat(
        id = "12693",
        token = "6603F148E4B0FDA74F2A353A",
        chatId = null,
        email = null,
        language = "zh-CN",
        userId = "89757000001ZW",
        extra =  hashMapOf("noBrand" to "true", "back_btn" to "true")
    )
```

+ step4. How to listen to Messages
```kotlin
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

+ step5. Run Demo in `com.rc.webcomponent.MainActivity`
