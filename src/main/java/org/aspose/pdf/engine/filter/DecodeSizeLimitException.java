package org.aspose.pdf.engine.filter;

import java.io.IOException;

/// Thrown when a decode filter's output exceeds [DecodeLimits#maxDecodedBytes()].
///
/// Distinct from a generic [IOException] so that error-tolerance paths
/// (FlateDecode's nowrap retry and tail-truncation salvage) do NOT treat a
/// decompression bomb as recoverable corruption: re-running the salvage
/// search over a multi-hundred-megabyte bomb would itself re-allocate the
/// capped buffer a dozen times. A size-limit failure is final.
///
public class DecodeSizeLimitException extends IOException {

    private static final long serialVersionUID = 1L;

    /// Creates the exception.
    ///
    /// @param message the diagnostic message
    public DecodeSizeLimitException(String message) {
        super(message);
    }
}
