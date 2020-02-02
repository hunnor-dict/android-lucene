package net.hunnor.dict.android.activity.settings;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class SettingsActivityTest {

    @Rule
    public ActivityTestRule<SettingsActivity> activityRule =
            new ActivityTestRule<>(SettingsActivity.class);

    @Test
    public void testSettingsActivity() {
        onView(withText("Találati lista mérete")).check(matches(isDisplayed()));
        onView(withText("Sötét mód")).check(matches(isDisplayed()));
    }

}
