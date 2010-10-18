package net.kenevans.gpxinspector.preferences;

import net.kenevans.gpxinspector.plugin.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
    implements IPreferenceConstants
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        prefs.setDefault(P_GPX_DIR, D_GPX_DIR);
        prefs.setDefault(P_STARTUP_FILES, D_STARTUP_FILES);
        prefs.setDefault(P_USE_STARTUP_FILES, D_USE_STARTUP_FILES);

        prefs.setDefault(P_KML_FILENAME, D_KML_FILENAME);
        prefs.setDefault(P_TRK_COLOR, D_TRK_COLOR);
        prefs.setDefault(P_TRK_ALPHA, D_TRK_ALPHA);
        prefs.setDefault(P_TRK_LINEWIDTH, D_TRK_LINEWIDTH);
        prefs.setDefault(P_TRK_COLOR_MODE, KML_COLOR_MODE_COLORSET);

        prefs.setDefault(P_ICON_SCALE, D_ICON_SCALE);
        prefs.setDefault(P_USE_TRK_ICON, D_USE_TRK_ICON);
        prefs.setDefault(P_TRK_ICON_URL, D_TRK_ICON_URL);
        prefs.setDefault(P_WPT_COLOR, D_WPT_COLOR);
        prefs.setDefault(P_WPT_ALPHA, D_WPT_ALPHA);
        prefs.setDefault(P_WPT_ICON_URL, D_WPT_ICON_URL);

        prefs.setDefault(P_KML_PROMPT_TO_OVERWRITE, D_KML_PROMPT_TO_OVERWRITE);
        prefs
            .setDefault(P_KML_SEND_TO_GOOGLE_EARTH, D_KML_SEND_TO_GOOGLE_EARTH);
    }

}
