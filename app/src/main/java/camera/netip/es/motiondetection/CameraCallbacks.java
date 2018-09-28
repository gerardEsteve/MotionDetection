package camera.netip.es.motiondetection;

import android.support.annotation.NonNull;

import java.io.File;

interface CameraCallbacks {

    void onImageCapture(@NonNull File imageFile);

    void onCameraError(@CameraError.CameraErrorCodes int errorCode);
}
