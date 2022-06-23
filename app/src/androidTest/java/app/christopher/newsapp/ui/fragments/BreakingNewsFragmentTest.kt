package app.christopher.newsapp.ui.fragments

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import app.christopher.newsapp.R
import app.christopher.newsapp.ui.NewsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class BreakingNewsFragmentTest {

    //Relaunches the application or activity
    @get: Rule
    val activityRule = ActivityScenarioRule(NewsActivity::class.java)
    private val LIST_ITEM_IN_TEST = 10

    /**
     * RecyclerView comes into view
     */
    @Test
    fun test_isRecyclerViewVisible_onAppLaunch() {
        val activityScenario = ActivityScenario.launch(NewsActivity::class.java)
        onView(withId(R.id.rvBreakingNews)).check(matches(isDisplayed()))
    }

    /**
     * Select list item, nav to ArticleFragment
     * Correct news article in view
     */
    @Test
    fun test_clickedListItem_isVisibleInArticleFragment() {
        val activityScenario = ActivityScenario.launch(NewsActivity::class.java)
        //onView(withId(R.id.rvBreakingNews)).perform(actionOnItemAtPosition<NewsAdapter.ArticleViewHolder>(LIST_ITEM_IN_TEST, click()))
    }
    /**
     * Select list item, nav to ArticleFragment
     * press Back
     */
    @Test
    fun test_backNavigation_toBreakingNewsFragment() {
        val activityScenario = ActivityScenario.launch(NewsActivity::class.java)
        //onView(withId(R.id.rvBreakingNews)).perform(actionOnItemAtPosition<NewsAdapter.ArticleViewHolder>(LIST_ITEM_IN_TEST, click()))
        pressBack()
    }
}