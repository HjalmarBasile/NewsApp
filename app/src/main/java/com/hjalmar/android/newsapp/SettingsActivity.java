package com.hjalmar.android.newsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final Preference.OnPreferenceChangeListener ON_PREFERENCE_CHANGE_LISTENER;

    static {
        ON_PREFERENCE_CHANGE_LISTENER = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String stringValue = newValue.toString();
                setPreferenceSummary(preference, stringValue);
                return true;
            }
        };
    }

    private static void setPreferenceSummary(Preference preference, String preferenceString) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(preferenceString);
            if (prefIndex >= 0) {
                CharSequence[] labels = listPreference.getEntries();
                preference.setSummary(labels[prefIndex]);
            }
        } else {
            preference.setSummary(preferenceString);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class NewsPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            // Retrieve all basic preferences information
            String[] settingsKeys = getResources().getStringArray(R.array.settings_keys);
            String[] settingsDefaults = getResources().getStringArray(R.array.settings_defaults);

            Preference[] preferences = new Preference[settingsKeys.length];
            for (int i = 0; i < settingsKeys.length; i++) {
                preferences[i] = findPreference(settingsKeys[i]);
            }

            setPreferencesSummary(preferences, settingsDefaults);
        }

        private void setPreferencesSummary(Preference[] preferences, String[] settingsDefaults) {

            if (preferences.length > 0) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preferences[0].getContext());

                for (int i = 0; i < preferences.length; i++) {
                    String preferenceString = sharedPreferences.getString(preferences[i].getKey(), settingsDefaults[i]);
                    setPreferenceSummary(preferences[i], preferenceString);
                    preferences[i].setOnPreferenceChangeListener(ON_PREFERENCE_CHANGE_LISTENER);
                }
            }
        }

    }

}
