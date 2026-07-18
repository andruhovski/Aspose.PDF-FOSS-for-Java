package org.aspose.pdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/// Represents a collection of [Artifact] objects found on a PDF page.
///
/// Artifacts are non-content elements such as headers, footers, watermarks,
/// and background images (ISO 32000-1:2008, §14.8.2.2). This collection
/// provides indexed access, iteration, and mutation operations.
///
public class ArtifactCollection implements Iterable<Artifact> {

    private static final Logger LOG = Logger.getLogger(ArtifactCollection.class.getName());

    private final List<Artifact> artifacts;

    /// Creates an empty artifact collection.
    public ArtifactCollection() {
        this.artifacts = new ArrayList<>();
    }

    /// Creates an artifact collection from an existing list.
    ///
    /// @param artifacts the initial artifacts
    /// @throws IllegalArgumentException if artifacts is null
    public ArtifactCollection(List<Artifact> artifacts) {
        if (artifacts == null) {
            throw new IllegalArgumentException("Artifacts list must not be null");
        }
        this.artifacts = new ArrayList<>(artifacts);
    }

    /// Returns the artifact at the specified 1-based index.
    ///
    /// @param index the 1-based index
    /// @return the artifact at the given index
    /// @throws IndexOutOfBoundsException if the index is out of range
    public Artifact get(int index) {
        if (index < 1 || index > artifacts.size()) {
            throw new IndexOutOfBoundsException(
                    "Artifact index " + index + " out of range [1, " + artifacts.size() + "]");
        }
        return artifacts.get(index - 1);
    }

    /// Returns the number of artifacts in this collection.
    ///
    /// @return the number of artifacts
    public int size() {
        return artifacts.size();
    }

    /// Adds an artifact to this collection.
    ///
    /// @param artifact the artifact to add
    /// @throws IllegalArgumentException if artifact is null
    public void add(Artifact artifact) {
        if (artifact == null) {
            throw new IllegalArgumentException("Artifact must not be null");
        }
        artifacts.add(artifact);
        LOG.fine(() -> "Artifact added, collection size=" + artifacts.size());
    }

    /// Removes the artifact at the specified 1-based index.
    ///
    /// @param index the 1-based index of the artifact to remove
    /// @throws IndexOutOfBoundsException if the index is out of range
    public void delete(int index) {
        if (index < 1 || index > artifacts.size()) {
            throw new IndexOutOfBoundsException(
                    "Artifact index " + index + " out of range [1, " + artifacts.size() + "]");
        }
        artifacts.remove(index - 1);
        LOG.fine(() -> "Artifact removed at index, collection size=" + artifacts.size());
    }

    /// Removes the specified artifact from this collection.
    ///
    /// @param artifact the artifact to remove
    /// @return `true` if the artifact was found and removed
    public boolean delete(Artifact artifact) {
        boolean removed = artifacts.remove(artifact);
        if (removed) {
            LOG.fine(() -> "Artifact removed, collection size=" + artifacts.size());
        }
        return removed;
    }

    /// Returns an iterator over the artifacts in this collection.
    ///
    /// @return an iterator
    @Override
    public Iterator<Artifact> iterator() {
        return artifacts.iterator();
    }

    /// Returns a string representation of this collection.
    ///
    /// @return a human-readable string
    @Override
    public String toString() {
        return "ArtifactCollection[size=" + artifacts.size() + "]";
    }
}
