package org.aspose.pdf;

import java.util.logging.Logger;

/// Controls optional font-substitution behavior used during standard-compliance
/// conversion and validation flows.
public class FontEmbeddingOptions {

    private static final Logger LOG = Logger.getLogger(FontEmbeddingOptions.class.getName());

    private boolean useDefaultSubstitution;

    /// Returns whether the converter may use a default substitution font when
    /// an expected source font cannot be resolved.
    ///
    /// @return true if default substitution is enabled
    public boolean isUseDefaultSubstitution() {
        return useDefaultSubstitution;
    }

    /// Sets whether the converter may use a default substitution font when
    /// an expected source font cannot be resolved.
    ///
    /// @param useDefaultSubstitution true to enable default substitution
    public void setUseDefaultSubstitution(boolean useDefaultSubstitution) {
        this.useDefaultSubstitution = useDefaultSubstitution;
        LOG.fine(() -> "Font default substitution set to " + useDefaultSubstitution);
    }
}
