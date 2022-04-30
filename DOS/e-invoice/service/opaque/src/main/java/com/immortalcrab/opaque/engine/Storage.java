package com.immortalcrab.opaque.engine;

import com.immortalcrab.opaque.error.StorageError;
import java.io.InputStream;

public interface Storage {

    public void upload(final String cType,
            final long len,
            final String fileName,
            InputStream inputStream) throws StorageError;
}
