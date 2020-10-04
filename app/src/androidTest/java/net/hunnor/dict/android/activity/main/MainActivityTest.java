package net.hunnor.dict.android.activity.main;

import androidx.test.rule.ActivityTestRule;

import net.hunnor.dict.android.R;
import net.hunnor.dict.android.model.Word;
import net.hunnor.dict.lucene.model.Entry;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testMainActivity() {

        onView(withId(R.id.search_input))
                .perform(click())
                .perform(typeText("eple"));

        onData(hasToString("eplekake"))
                .perform(click());

        onView(withId(R.id.search_list))
                .check(matches(isDisplayed()))
                .check(matches(hasChildCount(1)));

        onData(is(instanceOf(Entry.class)))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString("almáspite"))));

    }

}
