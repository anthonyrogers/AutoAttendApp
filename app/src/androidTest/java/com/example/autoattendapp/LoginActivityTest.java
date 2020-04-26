package com.example.autoattendapp;

import android.content.Context;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;


import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private String TEST_LOGIN_USER = "temple@temple.edu";
    private String TEST_LOGIN_PASS = "password";

    @Rule
    public ActivityTestRule<MainActivity> activityRule
            = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void loginWithUserAndPass() {
        onView(ViewMatchers.withId(R.id.emailTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_LOGIN_USER), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.passwordTxtBox))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(TEST_LOGIN_PASS), ViewActions.closeSoftKeyboard());

        onView(ViewMatchers.withId(R.id.loginButton))
                .perform(ViewActions.click());

        Intents.init();
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e){}
        intended(hasComponent(CourseListActivity.class.getName()));
        Intents.release();
    }
}
