# Page Rasterization

Convert PDF pages to image formats (PNG, JPEG, BMP, GIF, TIFF) using the device classes under `org.aspose.pdf.devices`.

## Quick example

Render the first page to a PNG file at 150 DPI:

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.devices.PngDevice;
import org.aspose.pdf.devices.Resolution;

import java.io.FileOutputStream;

try (Document doc = new Document("input.pdf")) {
    Page page = doc.getPages().get(1);

    PngDevice device = new PngDevice(new Resolution(150));
    try (FileOutputStream out = new FileOutputStream("page-1.png")) {
        device.process(page, out);
    }
}
```

## Devices available

| Device | Format | Notes |
|---|---|---|
| `PngDevice` | PNG | Lossless; preserves transparency |
| `JpegDevice` | JPEG | Lossy; smallest files for photographic content |
| `BmpDevice` | BMP | Uncompressed; very large files |
| `GifDevice` | GIF | 256-colour palette; good for line art |
| `TiffDevice` | TIFF | Multi-page; one TIFF can hold all PDF pages |

All except `TiffDevice` produce one image per page. `TiffDevice` can package multiple pages into a single multi-page TIFF.

## Resolution

`Resolution` controls the DPI of the rendered output:

```java
new Resolution(72);     // screen resolution (smallest)
new Resolution(150);    // typical for on-screen viewing
new Resolution(300);    // print quality
new Resolution(600);    // high-quality print, archival
```

Higher DPI produces larger, sharper images with linearly more memory and CPU cost. For most web use cases, 96–150 DPI is sufficient.

Asymmetric resolution (different horizontal and vertical DPI) is supported:

```java
new Resolution(300, 150);  // 300 DPI horizontal, 150 vertical
```

## Fixed-size output

If you need a specific pixel size regardless of the page's physical size, use the size-aware constructor:

```java
// Render to exactly 800 x 1200 pixels at 96 DPI
PngDevice device = new PngDevice(800, 1200, new Resolution(96));
```

The page contents are scaled to fit. Aspect ratio is preserved; if the requested size has a different aspect from the page, the page is centred and rendered at the largest size that fits.

## Render every page

```java
try (Document doc = new Document("input.pdf")) {
    PngDevice device = new PngDevice(new Resolution(150));
    for (int i = 1; i <= doc.getPages().getCount(); i++) {
        try (FileOutputStream out = new FileOutputStream("page-" + i + ".png")) {
            device.process(doc.getPages().get(i), out);
        }
    }
}
```

## Render to an in-memory buffer

```java
import java.io.ByteArrayOutputStream;

try (Document doc = new Document("input.pdf")) {
    PngDevice device = new PngDevice(new Resolution(96));
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    device.process(doc.getPages().get(1), buf);
    byte[] pngBytes = buf.toByteArray();
    // ... pass pngBytes to a web framework, encode as base64, etc.
}
```

## Multi-page TIFF

`TiffDevice` is designed to write all pages of a document into a single TIFF file:

```java
import org.aspose.pdf.devices.TiffDevice;

try (Document doc = new Document("input.pdf")) {
    TiffDevice device = new TiffDevice(new Resolution(150));
    try (FileOutputStream out = new FileOutputStream("output.tiff")) {
        // Render every page
        for (int i = 1; i <= doc.getPages().getCount(); i++) {
            device.process(doc.getPages().get(i), out);
        }
    }
}
```

See `TiffDevice` in the JavaDoc for additional constructors that take compression settings (`CompressionType.LZW`, `CompressionType.CCITT4`, etc.) and colour depth (`ColorDepth.Format8bpp`, `ColorDepth.Format1bpp` for black-and-white, ...).

## JPEG quality

For `JpegDevice`, you can set quality (0–100):

```java
import org.aspose.pdf.devices.JpegDevice;

JpegDevice device = new JpegDevice(new Resolution(150), 85);  // quality = 85
```

Default is around 95. For typical web use, 75–85 is a good trade-off between size and quality.

## Common patterns

### Generate thumbnails

```java
try (Document doc = new Document("input.pdf")) {
    // Small thumbnail, 200px wide, ~aspect-ratio-preserving height for a Letter page
    JpegDevice device = new JpegDevice(200, 260, new Resolution(72), 75);
    try (FileOutputStream out = new FileOutputStream("thumb.jpg")) {
        device.process(doc.getPages().get(1), out);
    }
}
```

### Export every page as PNG into a folder

```java
import java.nio.file.Files;
import java.nio.file.Path;

void renderAll(Path inputPdf, Path outputDir) throws Exception {
    Files.createDirectories(outputDir);
    try (Document doc = new Document(inputPdf.toString())) {
        PngDevice device = new PngDevice(new Resolution(150));
        int width = String.valueOf(doc.getPages().getCount()).length();

        for (int i = 1; i <= doc.getPages().getCount(); i++) {
            Path out = outputDir.resolve(String.format("page-%0" + width + "d.png", i));
            try (var os = Files.newOutputStream(out)) {
                device.process(doc.getPages().get(i), os);
            }
        }
    }
}
```

## Performance tips

- **Render lazily.** Don't render every page upfront if you only need the first. `process()` is per-page.
- **Reduce DPI for previews.** 72–96 DPI is enough for screen previews. Reserve 300+ DPI for print.
- **Reuse devices.** A `PngDevice` (or any subclass) can be reused across many `process()` calls; you don't need to instantiate a fresh one per page.
- **Watch heap on large pages.** A single A0 page at 600 DPI is around 5000 × 7000 pixels = 35 MP. Rendering uncompressed 4-channel takes ~140 MB just for the raster. Lower DPI or render to a tile-aware output if memory is a concern.

## Limitations

- **Some non-PDF rendering options are not implemented.** The commercial Aspose.PDF has many `RenderingOptions` for things like font hinting strategies, scale modes, OCG (optional content) visibility, etc. The FOSS edition implements a baseline; more options will land in future versions.
- **Form fields and annotations** are rasterized as part of the page using current appearances; widgets without explicit appearance streams may not look identical to viewers that regenerate appearances on the fly.
- **Transparency and blend modes**: most common cases work; some advanced PDF 1.4+ transparency edge cases may render differently from reference renderers like Adobe.
