package itkach.aard2.slob;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import itkach.aard2.SlobHelper;
import itkach.slob.Slob;

// This is a virtual server mimicking an actual server for our webview
// Format: host/slob/slobId/key?blob=<id>#fragment
// Format: host/slob/uri/key#fragment
// Returns: Content with designated types and caching info.
//
// Caching:
// For Slob ID, it is `Cache-Control: max-age=31556926`
// For URI, it is `Cache-Control: max-age=600`
//
// For URI, Slob ID is returned as ETag to reuse previous results.
public class SlobRequestHandler {
    @NonNull
    public static String getMimeType(@NonNull String contentType) {
        int semiColon = contentType.indexOf(';');
        return (semiColon == -1 ? contentType : contentType.substring(0, semiColon)).trim();
    }

    @NonNull
    public WebResourceResponse handleRequest(@NonNull WebResourceRequest request) {
        Uri uri = request.getUrl();
        List<String> pathSegments = uri.getPathSegments();
        // pathSegments: [0]="slob", [1]="slobIdOrUri", [2...]="key"
        if (pathSegments.size() < 3) {
            return respondWithError(404, "Not Found");
        }
        // pathSegments.get(0) is kept for compatibility
        String slobIdOrUri = pathSegments.get(1);
        // Reconstruct the key from remaining path segments
        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 2; i < pathSegments.size(); i++) {
            if (keyBuilder.length() > 0) keyBuilder.append("/");
            keyBuilder.append(pathSegments.get(i));
        }
        String key = keyBuilder.toString();
        // Extract If-None-Match header
        String ifNoneMatch = getHeaderCaseInsensitive(request.getRequestHeaders(), "If-None-Match");

        SlobHelper slobHelper = SlobHelper.getInstance();
        // Slob ID or URI check
        Slob slob = slobHelper.getSlob(slobIdOrUri);
        if (slob != null) {
            // Slob ID
            String blobId = uri.getQueryParameter("blob");
            if (blobId != null) {
                try {
                    Slob.Content content = slob.getContent(blobId);
                    return createResponse(content, "max-age=31556926", null);
                } catch (AssertionError e) {
                    // Invalid blobId, fall through to key lookup
                }
            }
            if (getEtag(slob.getId()).equals(ifNoneMatch)) {
                return respondWithNotModified();
            }
            Slob.Blob blob = getBlobByUri(slobHelper, slob, key);
            if (blob == null) {
                return respondWithError(404, "Not Found");
            }
            return createResponse(blob.getContent(), "max-age=31556926", null);
        }
        // Might be URI
        slob = slobHelper.findSlob(slobIdOrUri);
        if (slob == null) {
            return respondWithError(404, "Not Found");
        }
        if (getEtag(slob.getId()).equals(ifNoneMatch)) {
            return respondWithNotModified();
        }
        Slob.Blob blob = getBlobByUri(slobHelper, slob, key);
        if (blob == null) {
            return respondWithError(404, "Not Found");
        }
        return createResponse(blob.getContent(), "max-age=600", getEtag(slob.getId()));
    }

    @NonNull
    private String getEtag(UUID slobId) {
        return String.format(Locale.ROOT, "\"%s\"", slobId);
    }

    @Nullable
    private Slob.Blob getBlobByUri(@NonNull SlobHelper slobHelper, @NonNull Slob slob, @NonNull String key) {
        List<Slob> candidates = slobHelper.findSlobsByUri(slob.getURI());

        Collections.sort(candidates, (o1, o2) -> {
            String createTime1 = o1.getTags().get("created.at");
            String createTime2 = o2.getTags().get("created.at");
            if (createTime2 == null) createTime2 = "";
            if (createTime1 == null) createTime1 = "";
            return createTime2.compareTo(createTime1);
        });

        Iterator<Slob.Blob> result = Slob.find(key, candidates.toArray(new Slob[0]),
                slob, Slob.Strength.SECONDARY);
        return result.hasNext() ? result.next() : null;
    }

    @NonNull
    private WebResourceResponse createResponse(@NonNull Slob.Content content, @Nullable String cacheControl, @Nullable String eTag) {
        String mimeType = getMimeType(content.type);
        String encoding = getEncoding(content.type);
        InputStream is = new ByteBufferBackedInputStream(content.data);
        WebResourceResponse response = new WebResourceResponse(mimeType, encoding, is);
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        if (cacheControl != null) {
            headers.put("Cache-Control", cacheControl);
        }
        if (eTag != null) {
            headers.put("ETag", eTag);
        }
        response.setResponseHeaders(headers);
        return response;
    }

    @NonNull
    private String getEncoding(@NonNull String contentType) {
        int charsetIndex = contentType.toLowerCase(Locale.ROOT).indexOf("charset=");
        if (charsetIndex != -1) {
            return contentType.substring(charsetIndex + 8).trim();
        }
        return "UTF-8";
    }

    @NonNull
    private WebResourceResponse respondWithError(int statusCode, @NonNull String reasonPhrase) {
        String content = "<!DOCTYPE html><html><body><h3>" + reasonPhrase + "</h3></body></html>";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        WebResourceResponse response = new WebResourceResponse("text/html", "UTF-8", is);
        response.setStatusCodeAndReasonPhrase(statusCode, reasonPhrase);
        return response;
    }

    @NonNull
    private WebResourceResponse respondWithNotModified() {
        // A 304 response does not have a body
        InputStream is = new ByteArrayInputStream(new byte[0]);
        WebResourceResponse response = new WebResourceResponse("text/plain", "UTF-8", is);
        response.setStatusCodeAndReasonPhrase(304, "Not Modified");
        return response;
    }

    private String getHeaderCaseInsensitive(Map<String, String> headers, String key) {
        if (headers == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}