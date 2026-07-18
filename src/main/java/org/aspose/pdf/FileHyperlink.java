package org.aspose.pdf;

/// Hyperlink that launches an external file when the host paragraph is
/// activated. Maps to a PDF `/Link` annotation with a `/Launch`
/// action. Mirrors Aspose.PDF's `Aspose.Pdf.FileHyperlink`.
public class FileHyperlink extends Hyperlink {

    private String path;

    /// Creates an empty file hyperlink.
    public FileHyperlink() {
    }

    /// Creates a file hyperlink pointing at `path`.
    ///
    /// @param path the file path the link should launch
    public FileHyperlink(String path) {
        this.path = path;
    }

    /// @return the target file path
    public String getPath() {
        return path;
    }

    /// Sets the file path the link should launch.
    ///
    /// @param path the file path
    public void setPath(String path) {
        this.path = path;
    }
}
