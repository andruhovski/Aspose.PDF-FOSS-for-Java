package org.aspose.pdf.engine.linearization;

import org.aspose.pdf.engine.io.RandomAccessReader;

import java.io.IOException;
import java.util.logging.Logger;

/// Detects whether a PDF is linearized and validates the linearization.
/// ISO 32000-1:2008 §F.3.3: the linearization dictionary must appear
/// within the first 1024 bytes of the file.
public final class LinearizationDetector {

    private static final Logger LOG = Logger.getLogger(LinearizationDetector.class.getName());

    private LinearizationDetector() {}

    /// Checks if the PDF is linearized and returns its parameters.
    ///
    /// @param reader the random access reader for the PDF file
    /// @return the linearization parameters, or `null` if not linearized
    /// @throws IOException if an I/O error occurs
    public static LinearizationParams detect(RandomAccessReader reader) throws IOException {
        return LinearizationParams.detect(reader);
    }

    /// Validates that a linearized PDF has not been modified since linearization.
    /// Per §F.3.4: if /L does not match the actual file length, the linearization
    /// data is invalid and should be ignored.
    ///
    /// @param params the linearization parameters
    /// @param reader the random access reader
    /// @return `true` if linearization is still valid
    /// @throws IOException if an I/O error occurs
    public static boolean isValid(LinearizationParams params,
                                   RandomAccessReader reader) throws IOException {
        if (params == null) return false;
        return params.isValid(reader.getLength());
    }
}
