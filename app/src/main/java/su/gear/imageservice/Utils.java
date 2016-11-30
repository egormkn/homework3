package su.gear.imageservice;

import java.io.Closeable;

public final class Utils {

    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_LOADING = 2;

    private Utils() {}

    private static final String[] links = {
            "http://hdwallpaperbackgrounds.net/wp-content/uploads/2015/10/The-Crew-Wallpapers-HD.jpg",
            "http://hdwallpaperbackgrounds.net/wp-content/uploads/2015/08/Car-Racing-3D-Game-Desktop-Backgrounds.jpg",
            "http://hdwallpaperbackgrounds.net/wp-content/uploads/2015/08/Race-Car-3D-Game-Desktop-Backgrounds.jpg"

    };

    private static int last = 0;

    public static String getNextImageUrl() {
        return links[++last % links.length];
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {}
    }
}
