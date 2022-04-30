package com.immortalcrab.opaque.engine;

import com.immortalcrab.opaque.error.FormatError;
import com.immortalcrab.opaque.error.StorageError;

public interface StepPdf {

    public void render(final CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError;
}