package camera.netip.es.motiondetection.config;

import android.hardware.Camera;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class CameraFocus {

    /**
     * Camera should focus automatically. This is the default focus mode if the camera focus
     * is not set.
     *
     * @see Camera.Parameters#FOCUS_MODE_AUTO
     */
    public static final int AUTO = 0;
    /**
     * Camera should focus automatically.
     *
     * @see Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE
     */
    public static final int CONTINUOUS_PICTURE = 1;
    /**
     * Do not focus the camera.
     */
    public static final int NO_FOCUS = 2;

    private CameraFocus() {
        throw new RuntimeException("Cannot initialize this class.");
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({AUTO, CONTINUOUS_PICTURE, NO_FOCUS})
    public @interface SupportedCameraFocus {
    }
}
