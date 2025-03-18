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
            // if you has support AGP 8.X 
	        implementation 'com.github.VOC-AI:VOCLiveChat-Android:1.2.1'
            // if you only support AGP 7.x
            implementation 'com.github.VOC-AI:VOCLiveChat-Android:1.2.0.2'
	}
```

+ step3. launch chat sdk. `chatId`,`email`,`language` is nullable
```groovy
    Vocai.Companion.getInstance().init(context,false)
    Vocai.Companion.getInstance().startChat(id = "", token = "", chatId = "", email = "", language = "", extra = null)
```
noted: if you AGP is 7.x , please refer to https://github.com/VOC-AI/VOCLiveChat-Android/blob/feature/compat_gradle_7.x/app/src/main/java/com/rc/webcomponent/MainActivity.kt to integration our sdk.