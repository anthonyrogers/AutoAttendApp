package com.example.autoattendapp;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RegisterStudentActivityTest {

    private String TEST_REGISTER_TUID = "999999999";
    private String TEST_REGISTER_NAME_FIRST = "espresso";
    private String TEST_REGISTER_NAME_LAST = "test";
    private String TEST_REGISTER_USER = getSaltString() + "@temple.edu";
    private String TEST_REGISTER_PASS = "password";

    protected String getSaltString() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    @Rule
    public ActivityTestRule<MainActivity> activityRule
            = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void registerNewStudent() {
        onView(ViewMatchers.withId(R.id.createAccount))
                .perform(ViewActions.click());

        onView(ViewMatchers.withId(R.id.studentAccPic))
                .perform(ViewActions.click());

        onView(ViewMatchers.withId(R.id.TUIDtxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_REGISTER_TUID), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.firstNameTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_REGISTER_NAME_FIRST), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.lastNameTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_REGISTER_NAME_LAST), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.emailTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_REGISTER_USER), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.passwordTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_REGISTER_PASS), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.registerStudentBtn))
                .perform(ViewActions.click());

        Intents.init();
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e){}
        intended(hasComponent(CourseListActivity.class.getName()));
        Intents.release();
    }
}
