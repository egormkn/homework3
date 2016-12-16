package su.gear.imageservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import java.io.Closeable;
import java.util.Random;

public final class Utils {

    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_LOADING = 2;
    public static final int RESULT_STARTED = 3;

    private static final Random random = new Random();

    // Images from simonstalenhag.se
    private static final String[] links = {
            "http://simonstalenhag.se/bilderbig/thelan_1920.jpg",
            "http://simonstalenhag.se/bilderbig/summerlove_1920.jpg",
            "http://simonstalenhag.se/bilderbig/astronomen_1920.jpg",
            "http://simonstalenhag.se/bilderbig/astrovilla_1920.jpg",
            "http://simonstalenhag.se/bilderbig/by_usgs_2560.jpg",
            "http://simonstalenhag.se/bilderbig/by_josies_2560.jpg",
            "http://simonstalenhag.se/bilderbig/by_warmachines3_1920.jpg",
            "http://simonstalenhag.se/bilderbig/bio_cargrab_1920.jpg",
            "http://simonstalenhag.se/bilderbig/peripheral2_1920.jpg",
            "http://simonstalenhag.se/bilderbig/spanviken2_1920.jpg",
            "http://simonstalenhag.se/bilderbig/hackingtheloop_1920.jpg",
            "http://simonstalenhag.se/bilderbig/december1994_1920.jpg",
            "http://simonstalenhag.se/bilderbig/thehunch_1920.jpg",
            "http://simonstalenhag.se/bilderbig/by_warmachines3_1920.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/tyrannosaurus.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/proganochelys.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/lycaenops.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/robertia.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/plateosaurus.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/coelophysis.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/compsognathus.jpg",
            "http://www.simonstalenhag.se/bilderbig/paleo/ramphorynchus.jpg"
    };

    public static String getNextImageUrl() {
        return links[random.nextInt(links.length)];
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {}
    }

    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 10f;

    public static Bitmap blurBitmap(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);

        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);

        // There is no understandable documentation for ScriptGroup.Builder2 :(
        // Please, help me to change blurred image color

        /*ScriptIntrinsicColorMatrix theMatrix = ScriptIntrinsicColorMatrix.create(rs, Element.U8_4(rs));
        theMatrix.setAdd((float) -0.6, (float) -0.6, (float) -0.6, (float) 1.0);
        theMatrix.forEach(tmpIn, tmpOut);*/

        tmpOut.copyTo(outputBitmap);

        tmpIn.destroy();
        tmpOut.destroy();
        rs.destroy();

        return outputBitmap;
    }

    private Utils() {}
}
