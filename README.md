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
            implementation 'com.github.VOC-AI:VOCLiveChat-Android:1.2.5'
	}
```

+ step3. launch chat sdk. `chatId`,`email`,`language` is nullable
```java
    import com.vocai.sdk.Vocai.Companion as VocaiSDK

    VocaiSDK.getInstance().init(this,false) {
        Log.i("MainActivity","vocai sdk is canceled")
    }

    # 传入userId  唯一值，建议使用设备ID
    VocaiSDK.getInstance().startChat("","", null,"{email}", "{language}", "{userId}", hashMapOf(
            "email" to "xxxxxxx@shulex-tech.com"
    ))

    # 未读消息监听 hasUnread: Boolean
    VocaiMessageCenter.instance.subscribe { hasUnread -> {
	LogUtil.info("hasUnread:$hasUnread")
    } }

```
