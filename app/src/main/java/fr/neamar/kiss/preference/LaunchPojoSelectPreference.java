package fr.neamar.kiss.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.neamar.kiss.DataHandler;
import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.pojo.AppPojo;
import fr.neamar.kiss.pojo.NameComparator;
import fr.neamar.kiss.pojo.Pojo;
import fr.neamar.kiss.pojo.ShortcutPojo;

public class LaunchPojoSelectPreference extends ListPreference {
    public LaunchPojoSelectPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setEntries();
    }

    public LaunchPojoSelectPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setEntries();
    }

    public LaunchPojoSelectPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setEntries();
    }

    public LaunchPojoSelectPreference(@NonNull Context context) {
        super(context);
        setEntries();
    }

    private void setEntries() {
        DataHandler dataHandler = getDataHandler();

        List<Pojo> allPojos = new ArrayList<>();

        List<AppPojo> appPojoList = dataHandler.getApplications();
        if (appPojoList != null) {
            allPojos.addAll(appPojoList);
        }

        List<ShortcutPojo> shortcutPojoList = dataHandler.getPinnedShortcuts();
        if (shortcutPojoList != null) {
            allPojos.addAll(shortcutPojoList);
        }

        Collections.sort(allPojos, new NameComparator());

        // generate entry names and entry values
        final int count = allPojos.size();
        CharSequence[] entries = new CharSequence[count];
        CharSequence[] entryValues = new CharSequence[count];
        for (int idx = 0; idx < count; idx++) {
            Pojo entry = allPojos.get(idx);
            entries[idx] = entry.getName();
            entryValues[idx] = entry.id;
        }

        setEntries(entries);
        setEntryValues(entryValues);

        setEnabled(!allPojos.isEmpty());
    }

    private DataHandler getDataHandler() {
        return KissApplication.getApplication(getContext()).getDataHandler();
    }

}