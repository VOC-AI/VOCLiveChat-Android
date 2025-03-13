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
	        implementation 'com.github.VOC-AI:VOCLiveChat-Android:1.2.1'
	}
```

+ step3. launch chat sdk. `chatId`,`email`,`language` is nullable
```groovy
    Vocai.Companion.getInstance().init(context,false)
    Vocai.Companion.getInstance().startChat(id = "", token = "", chatId = "", email = "", language = "", extra = null)
```