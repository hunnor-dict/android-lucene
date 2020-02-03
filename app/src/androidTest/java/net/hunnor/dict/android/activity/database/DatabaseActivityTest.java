package net.hunnor.dict.android.activity.database;

import androidx.test.rule.ActivityTestRule;

import net.hunnor.dict.android.R;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class DatabaseActivityTest {

    @Rule
    public ActivityTestRule<DatabaseActivity> activityRule =
            new ActivityTestRule<>(DatabaseActivity.class);

    @Test
    public void testDatabaseActivity() {
        onView(withText(activityRule.getActivity().getResources().getString(
                R.string.database_local_title))).check(matches(isDisplayed()));
        onView(withText(activityRule.getActivity().getResources().getString(
                R.string.database_remote_title))).check(matches(isDisplayed()));
    }

}
