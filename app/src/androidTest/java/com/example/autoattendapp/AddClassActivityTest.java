package com.example.autoattendapp;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddClassActivityTest {

    private String POPUP_WINDOW_BACKGROUND_VIEW = "android.widget.PopupWindow$PopupBackgroundView";
    private String SCROLL_VIEW = "android.widget.ScrollView";
    private String OK_DIALOGUE = "OK";

    private String TEST_LOGIN_USER = "temple@temple.edu";
    private String TEST_LOGIN_PASS = "password";
    private String COURSE_NAME = getRandomName();
    private String LOCATION = getRandomName();
    private String START_TIME = "10:00 AM";
    private String END_TIME = "11:50 AM";

    protected String getRandomName() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz 0123456";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_COARSE_LOCATION");

    @Test
    public void addClassActivityTest() {

        onView(ViewMatchers.withId(R.id.emailTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_LOGIN_USER), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.passwordTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_LOGIN_PASS), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.loginButton))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e){}

        onView(ViewMatchers.withId(R.id.buttonAddCourse))
                .perform(ViewActions.click());

        onView(ViewMatchers.withId(R.id.editTextCourse))
                .perform(replaceText(COURSE_NAME), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.editTextLocation))
                .perform(replaceText(LOCATION), ViewActions.closeSoftKeyboard());

        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.spinWeekDay1),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatSpinner.perform(click());

        DataInteraction checkedTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is(POPUP_WINDOW_BACKGROUND_VIEW)),
                        0))
                .atPosition(1);
        checkedTextView.perform(click());

        ViewInteraction appCompatSpinner2 = onView(
                allOf(withId(R.id.spinWeekDay2),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                16),
                        isDisplayed()));
        appCompatSpinner2.perform(click());

        DataInteraction checkedTextView2 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is(POPUP_WINDOW_BACKGROUND_VIEW)),
                        0))
                .atPosition(3);
        checkedTextView2.perform(click());

        ViewInteraction appCompatSpinner3 = onView(
                allOf(withId(R.id.spinWeekDay3),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatSpinner3.perform(click());

        DataInteraction checkedTextView3 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is(POPUP_WINDOW_BACKGROUND_VIEW)),
                        0))
                .atPosition(5);
        checkedTextView3.perform(click());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.etStartTime1),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                15),
                        isDisplayed()));
        appCompatEditText5.perform(typeText(START_TIME));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is(SCROLL_VIEW)),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.etEndTime1),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        appCompatEditText6.perform(typeText(END_TIME));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is(SCROLL_VIEW)),
                                        0),
                                3)));
        appCompatButton4.perform(scrollTo(), click());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.etStartTime2),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                20),
                        isDisplayed()));
        appCompatEditText7.perform(typeText(START_TIME));

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is(SCROLL_VIEW)),
                                        0),
                                3)));
        appCompatButton5.perform(scrollTo(), click());

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.etEndTime2),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        appCompatEditText8.perform(typeText(END_TIME));

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is(SCROLL_VIEW)),
                                        0),
                                3)));
        appCompatButton6.perform(scrollTo(), click());

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.etStartTime3),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                17),
                        isDisplayed()));
        appCompatEditText9.perform(typeText(START_TIME));

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is(SCROLL_VIEW)),
                                        0),
                                3)));
        appCompatButton7.perform(scrollTo(), click());

        ViewInteraction appCompatEditText10 = onView(
                allOf(withId(R.id.etEndTime3),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                14),
                        isDisplayed()));
        appCompatEditText10.perform(typeText(END_TIME));

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is(SCROLL_VIEW)),
                                        0),
                                3)));
        appCompatButton8.perform(scrollTo(), click());

        ViewInteraction appCompatEditText11 = onView(
                allOf(withId(R.id.editTextStartDay),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                18),
                        isDisplayed()));
        appCompatEditText11.perform(click());

        ViewInteraction appCompatButton9 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is(SCROLL_VIEW)),
                                        0),
                                3)));
        appCompatButton9.perform(scrollTo(), click());

        ViewInteraction appCompatEditText12 = onView(
                allOf(withId(R.id.editTextEndDay),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));
        appCompatEditText12.perform(click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Next month"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.DayPickerView")),
                                        childAtPosition(
                                                withClassName(is("com.android.internal.widget.DialogViewAnimator")),
                                                0)),
                                2)));
        appCompatImageButton.perform(scrollTo(), click());

        ViewInteraction appCompatButton10 = onView(
                allOf(withId(android.R.id.button1), withText(OK_DIALOGUE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton10.perform(scrollTo(), click());

        ViewInteraction appCompatButton11 = onView(
                allOf(withId(R.id.addClassBtn), withText("Add Class"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                21),
                        isDisplayed()));
        appCompatButton11.perform(click());
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
