package com.example.wolfgao.mybakingapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wolfgao.mybakingapp.sync.MyBakingSyncAdaptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

//assertThat有两个默认的包，但是都不对，这是一个大坑，需要引入org.assertj.core.api.Assertions.assertThat参见
//https://stackoverflow.com/questions/33395275/importing-correct-assertthat-method-for-robolectric-test

//JUnit 4使用org.junit.*包而JUnit 3.8使用的是junit.framework.*
//可以静态地导入Assert类


/**
 * Here we are using a new Unit test framework: Robolectric, please see more information
 * http://robolectric.org/
 * It was recommended by Google, and very useful tool as good as Espresso.
 * 在简书上也有人对此有些总结，希望有帮助：
 * 写起单元测试来瞻前顾后，一方面单元测试需要运行在模拟器上或者真机上，麻烦而且缓慢，另一方面，一些依赖Android SDK
 * 的对象（如Activity，TextView等）的测试非常头疼，Robolectric可以解决此类问题，它的设计思路便是通过实现一套JVM能运行的Android代码，从而做到脱离Android环境进行测试。
 * https://www.jianshu.com/p/9d988a2f8ff7
 * Created by gaochuang on 2018/1/2.
 */

/**
 * 遭遇到了一个问题，不能run，后来search后获得解决方案
 * https://github.com/robolectric/robolectric/issues/1620
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = Config.class)
public class MainActivityUnitTest {

    private Activity mActivity;
    private ShadowActivity shadowActivity;
    private MockContentResolver mResolver;
    private MockContentProvider mProvider;
    private Context mContext;

    private CardView recipeCard;
    private TextView recipeName;
    private ImageView recipeImage;
    private TextView recipeIngre;

    private TextView emptyView;

    //Initializing all class instance variables.
    @Before
    public void setUp(){
        //不建议用setup来调用activity，但是用buildActivity总报空指针
        //mActivity = Robolectric.setupActivity(MainActivity.class);
        mContext = RuntimeEnvironment.application.getApplicationContext();
        MyBakingSyncAdaptor.initializeSyncAdapter(mContext);

        //如果想测整个生命周期，建议这样来获得:http://robolectric.org/activity-lifecycle/
        mActivity = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible().get();
        shadowActivity = Shadows.shadowOf(mActivity); //Robo 3.0


        mProvider = new MockContentProvider(mActivity);
        mResolver = new MockContentResolver(mActivity);

        assertNotNull(mActivity);
        recipeCard = (CardView) mActivity.findViewById(R.id.recipe_card);
        recipeName = (TextView) mActivity.findViewById(R.id.recipe_name);
        recipeImage = (ImageView) mActivity.findViewById(R.id.picture);
        recipeIngre = (TextView) mActivity.findViewById(R.id.recipe_ingredients);
        emptyView = (TextView) mActivity.findViewById(R.id.empty_view);
    }

    /**
     * 在JUnit4中，测试类不必再扩展junit.framework.TestCase；事实上，它们不必须扩展任何内容。但是，JUnit 4中
     * 使用的是注解。为了以一个测试用例方式执行，一个JUnit 4类中至少需要一个@Test注解。例如，如果你仅使用@Before
     * 和@After注解而没有至少提供一个@Test方法来编写一个类，那么，当你试图执行它时将得到一个错误。
     */
    @Test
    public void testEmptyView(){
        assertNotNull(emptyView);
        assertSame("Empty text is same", R.string.empty_recipe_list,emptyView.getText());
    }

    @Test
    public void testCardView(){
        assertNotNull(recipeCard);
        recipeCard.performClick();

        Intent intent = shadowActivity.peekNextStartedActivityForResult().intent;
        assertThat(intent.getComponent()).isEqualTo(new ComponentName(mActivity, DetailActivity.class));
        //assertThat(intent).hasComponent(new ComponentName(activity, DetailActivity.class));

    }
}
