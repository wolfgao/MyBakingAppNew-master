
## 简单说明： 
1）这个例子主要的设计模式是list view ->详细页面 ->视频页面；
2）主要用到的设计类有：RecyclerView, CardView, 这些view都在Fragment里面呈现；
3）因为要求用ExoPlayer，这个是最近谷歌推荐的非常流行的播放器，我们借鉴了demo的部分，这个player最牛逼的地方主要是两点：第一支持的sample格式齐全，第二可以自适应，根据带宽来调节不同的播放源，这个对现在的移动互联网视频网站简直就是福音，好好搞搞，挺有意思的。
4）因为要从网络获取数据，必须用到后台进程，目前Android有好几种办法，我们用了谷歌比较推荐的SyncSerice和SyncAdapter，这个和Cursor结合的话，基本满足大部分类似需求；
5）因为我们必须用Cursor来处理数据库，并且用了ContentProvider和ContentResolver，这部分花了不少精力终于搞明白他们的使用逻辑，的确有一些坑需要每个人去走一下才知道。
6）对于数据处理，如何建立数据库，数据表，和建立provider这部分有规范文档和实例，学起来比较快。
7）Widget的部分也是花了一些时间，尤其是获得appWidgetIds列表时发现是空，查找了很多资料搞定，参见下面详文。
8）用了第三方的一个类：RecyclerViewCursorAdapter，主要原因是作为RecycleView的Adapter不能实现CursorAdapter,后者只能用于ListView，这个给我带来了很多烦扰，后来发现这个第三方类，实际是继承了RecyclerView.Adapter，但是把CursorAdapter的方法都基本抄了一遍，不过这个类也work的很好。
9）最重要的单元测试，目前方案很多：Espresso, UiAutomator, JUnit4，还有一个新贵Robolectric,不过使用的时候发现这个框架对加载带有sync的应用总是fail，目前还在探索中。
笔者比较熟悉UiAutomator，这个更多是用来进行larger test的。
对于UI test，Espresso和
这里面[谷歌的官方文档](https://developer.android.com/training/testing/index.html)还是很牛的，多看看，再多看看 [Android Testing samples](https://github.com/googlesamples/android-testing).

最后，这个项目对我来说确实挑战比较大，主要是我没有多余时间，只能抽空来搞，而且断断续续，总体帮助还是很大。

## Configuration:
1. 增加了一个配置文件，在Assets目录下，名字叫做config.xml,里面主要是Server IP, Json文件路径，如下：
```
    <ip>http://05fa755e.ngrok.io</ip>
    <filePath>/Downloads/JSHome/Baking-site/baking.json</filePath>
```
2. 在AndroidManifest.xml文件里面的配置：
    1) 设置权限：
```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     <!-- Sync adaptor must need the 2 permissions  -->
    <uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
    <uses-permission android:name="android.permission.WRITE_SYNC_SETTINGS" />
    <uses-permission android:name="android.permission.READ_SYNC_SETTINGS" />
```
    2）设置了service：一个dummy authentication service和SyncAdapter service
    3）设置了一对provider和receiver，这个主要是针对Widget，当然针对widget还有一个特别的文件xx_widget_info.xml,在这里面主要是customize你的widget。
    4）style.xml文件主要是对你的app theme进行定制，这个也是很重要的文件；
3. 另外Json数据里面没有提高图片的链接，因此我不得不在本地Assets目录下放了4个图片，主要来显示菜单的图片，在assets目录下的文件不会编译。

## 关于宽屏的测试
本项目虽然要求了对宽屏的实现，但是由于精力所限，时间所限，没有完全完成这部分的代码，也没有测试，抱歉。

## 编程心得 

### 如何获取assets目录下的XML文件，并解析：
最主要的几个点：
1. xpp.setInput(applicationContext.getAssets().open(fileName),null);
2. xpp.getEventType()可以获取XML很多tag，通过对tag的判断来确定切入点。
    1. 主要有START_TAG/TEXT/END_TAG
    2. 判断文件结束时用END_DOCUMENT
3. 切记如果判断START_TA，就要用xpp.nextText()来获取tag之间的文本，如果用getText()就会返回null。
4. 如果判断TEXT，可以使用getText().

### 如何异步获取数据，而且和主线程（UI线程）分开，最终还是采用了SyncAdapter和SyncService，这个处理比AsyncTask类好的地方在于提供的接口更多，而且不是只执行一次，可以配置执行频率，这个效果非常好。核心的地方：
    1）必须要设立一个dummy账户来进行验证，参见[谷歌文档](http://developer.android.com/training/sync-adapters/creating-authenticator.html),否则就会抛security异常；
    2）这个账户的信息可以在一个xml文档里面保存，主要有contentAuthorityL，accountType
    3）验证账户的也是一个后台服务；
    4）在SyncAdapter里面完成了主要的任务：从网络读取数据，数据库写操作



### 数据网络获取异常，考虑UI界面的人性化，必须增加一个Empty view的处理，这个可以参见Sunshine项目的处理
    针对网络状况的检查，对server response code的处理，在空白页都要加上。

### RecyclerView和CardView的使用
这部分上整个UI显示的核心程序，RecyclerView是ListView的下一代，继承了很多ListView的方法，但是又增加了上滑下滑自动加载和消除itemView的机制，把它看作一个大的容器，itemView可以是CardView或者其他, 提供了一种插拔式的体验，高度的解耦，异常的灵活，通过设置它提供的不同LayoutManager，ItemDecoration , ItemAnimator实现令人瞠目的效果。
    - 你想要控制其显示的方式，请通过布局管理器LayoutManager
    - 你想要控制Item间的间隔（可绘制），请通过ItemDecoration
    - 你想要控制Item增删的动画，请通过ItemAnimator
    - 你想要控制点击、长按事件，请完成相关接口
    如何设置分割线，请参阅[相关文章](http://www.jianshu.com/p/b46a4ff7c10a)
 需要用到的方法只有两个：
 1.绘制分割线 public void onDraw(Canvas c, RecyclerView parent, State state)；
 2.设置偏移量 public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state)；

### 如何使用Exoplayer来进行视频的播放
参见官方说明，比我写的好：
ExoPlayer is an application level media player for Android. It provides an alternative to Android’s MediaPlayer API for playing audio and video both locally and over the Internet. ExoPlayer supports features not currently supported by Android’s MediaPlayer API, including DASH and SmoothStreaming adaptive playbacks. Unlike the MediaPlayer API, ExoPlayer is easy to customize and extend, and can be updated through Play Store application updates.
我这边的实际总结：
1. 参考demo程序就可以基本搞定了：主要是DataSource，trackSelector，SimpleExoPlayerView；
```
    // 1. Create a default TrackSelector
    TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
    trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            ...
    // 2. Create the player
    boolean preferExtensionDecoders = mIntent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
    @DefaultRenderersFactory.ExtensionRendererMode
    int extensionRendererMode =DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
    DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(mContext,
            null, extensionRendererMode);

    player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

    // 3. build MediaSource
    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        ...
    }

    // 4. prepare和play 
     player.prepare(mediaSource, !haveResumePosition, false);
```
2. ![面向对象的设计]()

### 如何在Activity和自己的Fragment直接传递参数
1、第一种方式，也是最常用的方式，就是使用Bundle来传递参数,也是本项目采用的方式。
```
    MyFragment myFragment = new MyFragment();
        Bundle bundle = new Bundle();
        bundle.putString("DATA",values);//这里的values就是我们要传的值
        myFragment.setArguments(bundle);
```
然后在Fragment中的onCreatView方法中，通过getArgments()方法，获取到bundle对象，然后通过getString的key值拿到我们传递过来的值。

2、第二种方式，是在宿主Activity中定义方法，将要传递的值传递到Fragment中，在Fragment中的onAttach方法中，获取到这个值。
```
//宿主activity中的getTitles()方法
public String getTitles(){
    return "hello";
}

//Fragment中的onAttach方法
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        titles = ((MainActivity) activity).getTitles();
    }
//通过强转成宿主activity，就可以获取到传递过来的数据
```

3、下面在扩展一下创建Fragment和传递数值
如果我们不需要传递数值，那就直接可以在宿主activity中，跟平常一样创建fragment，但是如果我们需要传递数据的话，可以使用newInstance（数据）方法来传递，这个方法是自己定义的，但是是定义在Fragment中的一个静态方法。

### Loader机制，我们采用了loader来获取数据的变更非常有用。它可以方便我们在Activity和Fragment中异步加载数据，而不是用线程或AsyncTask，他的优点如下：
#### Loader Manager的有点
* 提供异步加载数据机制；
* 对数据源变化进行监听，实时更新数据；
* 在Activity配置发生变化（如横竖屏切换）时不用重复加载数据；
* 适用于任何Activity和Fragment；
#### 在应用中使用Loader
在我们开发的一个App里，使用Loader时常规的步骤包含如下一些操作需求：
* 一个Activity或Fragment；
* 一个LoaderManager实例；
* 一个CursorLoader，从ContentProvider加载数据；
* 一个LoaderManager.LoaderCallbacks实现，创建新Loader及管理已存在Loader；
* 一个组织Loader数据的Adapter，如SimpleCursorAdapter；

#### 对Uri的理解非常重要：
projection  要返回的列key list，null表示返回所有列，但是返回所有列很多时候会降低性能
selection   要返回的行过滤，也就是SQL中的WHERE语句，null代表返回uri指定的所有行
selectionArgs   用来替换上面selection中包含的＂？＂
sortOrder   结果的行排序，也就是SQL中的ORDER BY，传递null则无序

### Widget的收获：主要是一个发送消息，一个接受消息，只要这两部分代码能对上，就问题不大
发送部分：
```
    // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(mContext, MainActivity.class);
        dataUpdatedIntent.setPackage(mContext.getPackageName());
        dataUpdatedIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        mContext.sendBroadcast(dataUpdatedIntent);
```
接受部分：
这地方发生了appWidgetIds列表返回为空，后来查了一下，很多人都有此问题，最终在[这里找到答案](https://stackoverflow.com/questions/20273543/appwidgetmanager-getappwidgetids-in-activity-returns-an-empty-list)
```
    public void onReceive(@NonNull Context context, @NonNull Intent intent){
        super.onReceive(context, intent);
        if(MyRecycleAdapter.APPWIDGET_UPDATE.equals(intent.getAction())){
            context.startService(new Intent(context, MyBakingWidgetIntentService.class));
        }
    }
```

### 最后就是关于UI test，这部分坑也比较多，谷歌虽然一直在推广small test，但是在实际的应用中，这部分可做的事情不是很多，比如检查view，检查位置，检查基本逻辑次序，检查数据库。
#### 我先搞了最新的框架Robolectric，发现每次运行都不能起来应用，总是报空指针，官方网站目前[帮助文档](http://robolectric.org/)不多,还得靠自己一点点查。
包括就assertThat这个类，就有三个包，最后发现在一个比较怪的包里面“org.assertj.core.api.Assertions.assertThat”；
另外，还有就是run不起来，发现很多人都遇到这个问题，参见[解决方案](https://github.com/robolectric/robolectric/issues/1620)，目前看比较推荐命令行来执行。
但是总体来说，这个比较轻，不需要特别的emulator环境，应该符合TDD的要求，长期看值得推荐和发展。

因此最后用了相对比较成熟的Esspreso，这个框架是比较成熟的[UI test框架](https://developer.android.com/training/testing/espresso/index.html)。
1。 使用ActivityTestRule来创建Espresso测试用例；
 * 2。视图匹配：利用 Espresso.onView() 方法，您可以访问目标应用中的 UI 组件并与之交互。此方法接受 Matcher 参数
 * 并搜索视图层次结构，以找到符合给定条件的相应 View 实例：
 *      视图的类名称；
 *      视图的内容描述；
 *      视图的 R.id
 *      在视图中显示的文本
 * 3. 如何操作：
 * 通常情况下，您可以通过根据应用的用户界面执行某些用户交互来测试应用。借助 ViewActions API，您可以轻松地实现这些操作的自动化。您可以执行多种 UI 交互，例如：
     ViewActions.click(): 点击事件
     ViewActions.typeText(): 输入指定的文字内容
     ViewActions.scrollTo(): 滑动
     ViewActions.pressKey(): 按下按键
     ViewActions.clearText(): 清空文本
 * 4. 其实重要的是如何校验结果？

最后，在Android开发过程中，总会遇到这样那样的问题，要坚定信心，不断看文档和尝试，最终都能搞定，希望大家好运并能继续前进！！！


