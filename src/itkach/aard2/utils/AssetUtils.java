package itkach.aard2.utils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import itkach.aard2.Application;

public final class AssetUtils {
    private static final Map<String, String> cachedAssets = new HashMap<>();

    private AssetUtils() {
    }

    @NonNull
    public static String getAssetAsString(String assetName) {
        String data = cachedAssets.get(assetName);

        if (data == null) {
            try (InputStream is = Application.get().getAssets().open(assetName)) {
                data = Utils.readStream(is, 0);
            } catch (IOException e) {
                data = "";
            }
            cachedAssets.put(assetName, data);
        }
        return data;
    }
}
