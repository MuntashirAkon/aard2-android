package itkach.aard2.article;

import android.database.DataSetObserver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import itkach.aard2.lookup.LookupResult;
import itkach.slob.Slob;

class LookupResultWrapper implements BlobListWrapper {

    protected static final String TAG = "AARD-LRW";

    interface ToBlob<T> {
        @Nullable
        Slob.Blob convert(T item);
    }

    private final LookupResult lookupResult;
    private final ToBlob<Slob.Blob> toBlob;

    LookupResultWrapper(@NonNull LookupResult lookupResult, @NonNull ToBlob<Slob.Blob> toBlob) {
        this.lookupResult = lookupResult;
        this.toBlob = toBlob;
    }

    @Override
    public void registerDataSetObserver(@NonNull DataSetObserver observer) {
        lookupResult.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
        lookupResult.unregisterDataSetObserver(observer);
    }

    @Nullable
    @Override
    public Slob.Blob get(int index) {
        return toBlob.convert(lookupResult.getList().get(index));
        // TODO: Load more items?
    }

    @Nullable
    @Override
    public CharSequence getLabel(int index) {
        Slob.Blob item = null;
        if(index < lookupResult.getList().size()) {
            item = lookupResult.getList().get(index);
        } else {
            Log.e(TAG, "getLabel() invalid array index");
        }
        return item != null ? item.key : null;
    }

    @Override
    public int size() {
        return lookupResult.getList().size();
    }
}
