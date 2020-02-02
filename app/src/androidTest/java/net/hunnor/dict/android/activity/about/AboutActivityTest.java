package net.hunnor.dict.android.activity.about;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class AboutActivityTest {

    @Rule
    public ActivityTestRule<AboutActivity> activityRule =
            new ActivityTestRule<>(AboutActivity.class);

    @Test
    public void testAboutActivity() {
        onView(withText("HunNor")).check(matches(isDisplayed()));
        onView(withText("Forráskód")).check(matches(isDisplayed()));
        onView(withText("Ikon")).check(matches(isDisplayed()));
    }

}
