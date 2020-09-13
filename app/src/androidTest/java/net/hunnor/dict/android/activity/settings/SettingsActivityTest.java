package net.hunnor.dict.android.activity.settings;

import androidx.test.rule.ActivityTestRule;

import net.hunnor.dict.android.R;

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
        onView(withText(activityRule.getActivity().getResources().getString(
                R.string.settings_app_theme_title))).check(matches(isDisplayed()));
        onView(withText(activityRule.getActivity().getResources().getString(
                R.string.settings_max_words))).check(matches(isDisplayed()));
    }

}
