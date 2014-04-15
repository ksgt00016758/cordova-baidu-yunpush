Android配置说明:


1) Activity增加初始化调用：
FrontiaApplication.initFrontiaApplication(getBaseContext());

```xml
package com.funton.halfcity;

import android.os.Bundle;
import org.apache.cordova.*;

import com.baidu.frontia.FrontiaApplication;

public class HalfCity extends CordovaActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();
        // Set by <content src="index.html" /> in config.xml
        super.loadUrl(Config.getStartUrl());
        //super.loadUrl("file:///android_asset/www/index.html");
        FrontiaApplication.initFrontiaApplication(getBaseContext());
    }
}
```

2) AndroidManifest.xml:
activity增加：android:launchMode="singleTask"
```xml

<activity  ... android:launchMode="singleTask">

<!-- Push service 运行需要的权限 --/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE" /> 
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> 
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" /> 
<uses-permission android:name="android.permission.WRITE_SETTINGS" /> 
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> 
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" /> 
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

```

3） 引入android_lib下面的包

IOS配置说明：

1. 将 libs下面的包和文件加载到工程中
2. 工程必须引用的库:
    Foundation.framework 
    CoreTelephony.framework 
    SystemConfiguration.framework 
    libz.dylib
3. 创建并配置 BPushConfig.plist 文件
    在工程中创建一个新的 Property List 文件,并命名为 BPushConfig.plist,添加以下键值: ￼
    “API_KEY” = “pDUCHGTbD346jt2klpHRjHp7” “PRODUCTION_MODE” = NO
    “DEBUG” = NO
    “BPUSH_CHANNEL” = “91”
    API_KEY:必选。百度开发者中心为每个 app 自动分配的 api key,在开发者中心 app 基本信息 中可以查看。
    PRODUCTION_MODE:必选。应用发布模式。开发证书签名时,值设为”NO”;发布证书签名时, 值设为”YES”。请在调试和发布应用时,修改正确设置这个值,以免出现推送通知无法到达。
    DEBUG:可选。Push SDK 调试模式开关,值为 YES 时,将打开 SDK 日志。 BPUSH_CHANNEL:可选。渠道号,云推送将会进行统计,在控制台可以看到统计结果

4. Application文件需要引入必须的方法

```xml
#import <UIKit/UIKit.h>

#import <Cordova/CDVViewController.h>

@interface AppDelegate : NSObject <UIApplicationDelegate>{}

// invoke string is passed to your app on launch, this is only valid if you
// edit HalfCity-Info.plist to add a protocol
// a simple tutorial can be found here :
// http://iphonedevelopertips.com/cocoa/launching-your-own-application-via-a-custom-url-scheme.html

@property (nonatomic, strong) IBOutlet UIWindow* window;
@property (nonatomic, strong) IBOutlet CDVViewController* viewController;
@property (nonatomic, retain) NSDictionary	*launchNotification;

@end

#import "AppDelegate.h"
#import "MainViewController.h"
#import "BPush.h"
#import <Cordova/CDVPlugin.h>
#import "CDVPushPlugin.h"
#import <objc/runtime.h>

@implementation AppDelegate

@synthesize window, viewController;

- (id)init
{
    /** If you need to do any extra app-specific initialization, you can do it here
     *  -jm
     **/
    NSHTTPCookieStorage* cookieStorage = [NSHTTPCookieStorage sharedHTTPCookieStorage];

    [cookieStorage setCookieAcceptPolicy:NSHTTPCookieAcceptPolicyAlways];

    int cacheSizeMemory = 8 * 1024 * 1024; // 8MB
    int cacheSizeDisk = 32 * 1024 * 1024; // 32MB
#if __has_feature(objc_arc)
        NSURLCache* sharedCache = [[NSURLCache alloc] initWithMemoryCapacity:cacheSizeMemory diskCapacity:cacheSizeDisk diskPath:@"nsurlcache"];
#else
        NSURLCache* sharedCache = [[[NSURLCache alloc] initWithMemoryCapacity:cacheSizeMemory diskCapacity:cacheSizeDisk diskPath:@"nsurlcache"] autorelease];
#endif
    [NSURLCache setSharedURLCache:sharedCache];

    self = [super init];
    return self;
}

#pragma mark UIApplicationDelegate implementation

/**
 * This is main kick off after the app inits, the views and Settings are setup here. (preferred - iOS4 and up)
 */
- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
{
    NSLog(@"didFinishLaunchingWithOptions:%@", @"didFinishLaunchingWithOptions");
    CGRect screenBounds = [[UIScreen mainScreen] bounds];

#if __has_feature(objc_arc)
        self.window = [[UIWindow alloc] initWithFrame:screenBounds];
#else
        self.window = [[[UIWindow alloc] initWithFrame:screenBounds] autorelease];
#endif
    self.window.autoresizesSubviews = YES;

#if __has_feature(objc_arc)
        self.viewController = [[MainViewController alloc] init];
#else
        self.viewController = [[[MainViewController alloc] init] autorelease];
#endif

    // Set your app's start page by setting the <content src='foo.html' /> tag in config.xml.
    // If necessary, uncomment the line below to override it.
    // self.viewController.startPage = @"index.html";

    // NOTE: To customize the view's frame size (which defaults to full screen), override
    // [self.viewController viewWillAppear:] in your view controller.

    self.window.rootViewController = self.viewController;
    [self.window makeKeyAndVisible];
    
    [BPush setupChannel:launchOptions]; // 必须
    
    [BPush setDelegate:self]; // 必须。参数对象必须实现onMethod: response:方法，本示例中为self
    
    // [BPush setAccessToken:@"3.ad0c16fa2c6aa378f450f54adb08039.2592000.1367133742.282335-602025"];  // 可选。api key绑定时不需要，也可在其它时机调用
    
    [application registerForRemoteNotificationTypes:
     UIRemoteNotificationTypeAlert
     | UIRemoteNotificationTypeBadge
     | UIRemoteNotificationTypeSound];
    
    return YES;
}

// this happens while we are running ( in the background, or from within our own app )
// only valid if HalfCity-Info.plist specifies a protocol to handle
- (BOOL)application:(UIApplication*)application handleOpenURL:(NSURL*)url
{
    NSLog(@"handleOpenURL:%@", @"handleOpenURL");
    if (!url) {
        return NO;
    }

    // calls into javascript global function 'handleOpenURL'
    NSString* jsString = [NSString stringWithFormat:@"handleOpenURL(\"%@\");", url];
    [self.viewController.webView stringByEvaluatingJavaScriptFromString:jsString];

    // all plugins will get the notification, and their handlers will be called
    [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:CDVPluginHandleOpenURLNotification object:url]];

    return YES;
}

// repost the localnotification using the default NSNotificationCenter so multiple plugins may respond
- (void)            application:(UIApplication*)application
    didReceiveLocalNotification:(UILocalNotification*)notification
{
    // re-post ( broadcast )
    [[NSNotificationCenter defaultCenter] postNotificationName:CDVLocalNotification object:notification];
}

- (NSUInteger)application:(UIApplication*)application supportedInterfaceOrientationsForWindow:(UIWindow*)window
{
    // iPhone doesn't support upside down by default, while the iPad does.  Override to allow all orientations always, and let the root view controller decide what's allowed (the supported orientations mask gets intersected).
    NSUInteger supportedInterfaceOrientations = (1 << UIInterfaceOrientationPortrait) | (1 << UIInterfaceOrientationLandscapeLeft) | (1 << UIInterfaceOrientationLandscapeRight) | (1 << UIInterfaceOrientationPortraitUpsideDown);

    return supportedInterfaceOrientations;
}

- (void)applicationDidReceiveMemoryWarning:(UIApplication*)application
{
    [[NSURLCache sharedURLCache] removeAllCachedResponses];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    
    [BPush registerDeviceToken:deviceToken]; // 必须
    
    [BPush bindChannel]; // 必须。可以在其它时机调用，只有在该方法返回（通过onMethod:response:回调）绑定成功时，app才能接收到Push消息。一个app绑定成功至少一次即可（如果access token变更请重新绑定）。
}

- (void) onMethod:(NSString*)method response:(NSDictionary*)data
{
    NSLog(@"On method:%@", method);
    if ([BPushRequestMethod_Bind isEqualToString:method])
    {
        NSDictionary* res = [[NSDictionary alloc] initWithDictionary:data];
    }
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{

    NSString *notify_url = [userInfo valueForKey:@"notify_url"];
    //[self.viewController.webView stringByEvaluatingJavaScriptFromString:@"myJavascriptFunction()"];
    NSLog(@"ReceiveRemoteNotification:%@", userInfo);
    NSLog(@"ReceiveRemoteNotification:%@", notify_url);
    //[[NSNotificationCenter defaultCenter] postNotificationName:@"url" object:nil userInfo:userInfo];
     self.launchNotification = userInfo;
    [BPush handleNotification:userInfo]; // 可选
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    NSLog(@"applicationWillEnterForeground:%@", @"applicationWillEnterForeground");
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     NSLog(@"applicationDidBecomeActive:%@", @"applicationDidBecomeActive");
     //[self.viewController.webView stringByEvaluatingJavaScriptFromString:@"myJavascriptFunction()"];
    NSLog(@"active");
    
    //zero badge
    application.applicationIconBadgeNumber = 0;
    
    if (![self.viewController.webView isLoading] && self.launchNotification) {
        CDVPushPlugin *pushHandler = [self getCommandInstance:@"FGPushNotification"];
        
        pushHandler.notificationMessage = self.launchNotification;
        self.launchNotification = nil;
        [pushHandler performSelectorOnMainThread:@selector(notificationReceived) withObject:pushHandler waitUntilDone:NO];
    }
}
- (id) getCommandInstance:(NSString*)className
{
    return [self.viewController getCommandInstance:className];
}
// its dangerous to override a method from within a category.
// Instead we will use method swizzling. we set this up in the load call.
+ (void)load
{
    Method original, swizzled;
    
    original = class_getInstanceMethod(self, @selector(init));
    swizzled = class_getInstanceMethod(self, @selector(swizzled_init));
    method_exchangeImplementations(original, swizzled);
}

- (AppDelegate *)swizzled_init
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(createNotificationChecker:)
                                                 name:@"UIApplicationDidFinishLaunchingNotification" object:nil];
    
    // This actually calls the original init method over in AppDelegate. Equivilent to calling super
    // on an overrided method, this is not recursive, although it appears that way. neat huh?
    return [self swizzled_init];
}

// This code will be called immediately after application:didFinishLaunchingWithOptions:. We need
// to process notifications in cold-start situations
- (void)createNotificationChecker:(NSNotification *)notification
{
    if (notification)
    {
        NSDictionary *launchOptions = [notification userInfo];
        if (launchOptions)
            self.launchNotification = [launchOptions objectForKey: @"UIApplicationLaunchOptionsRemoteNotificationKey"];
    }
}


@end

```

JS中的引用：

```xml
    onDeviceReady: function() {
        navigator.splashscreen.hide();  
        app.receivedEvent('deviceready');
        if (device.platform == 'iOS' && parseFloat(window.device.version) >= 7.0) {
            $('.container').addClass('container-ios7');
            //platform = 'iOS';
        }
        if (device.platform == 'iOS'){
            platform = 'iOS';
        }
        fastgoPushNotification.register({
                                        "api_key":"ZVeSPDQtzWyGsSyyBzP242ec",
                                        "badge":"true",
                                        "sound":"true",
                                        "alert":"true",
                                        "ecb":"onNotification"
                                        });
        
    }

    function onNotification (event) {
        alert('RRR');
        INDEX_SWIPER.swipeNext();
        if ( event.alert )
        {
            
        }
        
        if ( event.sound )
        {
            
        }
        
        if ( event.badge )
        {   alert('R3');
        }
    }

```

