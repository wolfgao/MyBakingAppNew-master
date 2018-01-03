package com.example.wolfgao.mybakingapp;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;

/**
 * 关于Espresso的部分，参考了简书 https://www.jianshu.com/p/37f1897df3fd
 * 官方API： https://developer.android.com/reference/android/support/test/espresso/package-summary.html
 * 官方培训文档：https://developer.android.com/training/testing/espresso/index.html
 * 使用ActivityTestRule创建Espresso的步骤是，
 * 1。 使用ActivityTestRule来创建Espresso测试用例；
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
 基本上调用ViewInteraction.check()和DataInteraction.check()方法，可以判断UI元素的状态，如果断言失败，会抛出AssertionFailedError异常。
 比如：
 doesNotExist: 断言某一个view不存在
 matches: 断言某个view存在，且符合一列的匹配，也可以matches(withText(STRING_TO_BE_TYPED));
 selectedDescendentsMatch :断言指定的子元素存在，且他们的状态符合一些列的匹配

 Interacting with recycler view list items
 RecyclerView objects work differently than AdapterView objects, so onData() cannot be used to interact with them.

 To interact with RecyclerViews using Espresso, you can use the espresso-contrib package, which has a
 collection of RecyclerViewActions that can be used to scroll to positions or to perform actions on items:

 scrollTo() - Scrolls to the matched View.
 scrollToHolder() - Scrolls to the matched View Holder.
 scrollToPosition() - Scrolls to a specific position.
 actionOnHolderItem() - Performs a View Action on a matched View Holder.
 actionOnItem() - Performs a View Action on a matched View.
 actionOnItemAtPosition() - Performs a ViewAction on a view at a specific position.
参见：https://github.com/googlesamples/android-testing/tree/master/ui/espresso/RecyclerViewSample
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private MainActivity mainActivity;
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setActivity() {
        mainActivity = mActivityTestRule.getActivity();
    }
    //如果数据可以出来
    @Test
    public void recyclerViewTest(){
        //获得RecyclerView
        ViewInteraction firstItem = onView(withId(R.id.recycler_main_page))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        ViewInteraction detailView = onView(
                CoreMatchers.allOf(withId(R.id.recipe_detail_recyclerview),
                withParent(withId(R.id.recipe_detail_view)),
                isDisplayed()));

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void itemSelectTest() {
        onView(withId(R.id.recycler_main_page))
                .perform(RecyclerViewActions.actionOnHolderItem(
                        new CustomViewHolderMatcher(hasDescendant(withText("Nutella Pie"))), click()));

    }


    private static class CustomViewHolderMatcher extends TypeSafeMatcher<RecyclerView.ViewHolder> {
        private Matcher<View> itemMatcher = CoreMatchers.any(View.class);

        public CustomViewHolderMatcher() { }

        public CustomViewHolderMatcher(Matcher<View> itemMatcher) {
            this.itemMatcher = itemMatcher;
        }

        @Override
        public boolean matchesSafely(RecyclerView.ViewHolder viewHolder) {
            return MyRecycleAdapter.RecipeViewHolder.class.isAssignableFrom(viewHolder.getClass())
                    && itemMatcher.matches(viewHolder.itemView);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is assignable from CustomViewHolder");
        }
    }

    @Test
    public void detailViewTest(){
        //获得detailView第一个item，应该是step_short
        ViewInteraction step_short = onView(withId(R.id.recipe_detail_recyclerview)).
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }


}
