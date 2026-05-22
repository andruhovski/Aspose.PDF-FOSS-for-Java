package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

/**
 * Absorbs (finds) all image placements on PDF pages.
 * <p>
 * Processes the content stream to track the current transformation matrix (CTM),
 * and records each image XObject invocation (Do operator) with its CTM.
 * </p>
 * <p>Usage:
 * <pre>
 *   ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
 *   page.accept(absorber);
 *   List&lt;ImagePlacement&gt; placements = absorber.getImagePlacements();
 * </pre>
 * </p>
 */
public class ImagePlacementAbsorber {

    private static final Logger LOG = Logger.getLogger(ImagePlacementAbsorber.class.getName());

    private final List<ImagePlacement> placements = new ArrayList<>();

    /**
     * Visits a page and finds all image placements.
     *
     * @param page the PDF page to process
     * @throws IOException if content stream processing fails
     */
    public void visit(Page page) throws IOException {
        Resources res = page.getResources();
        if (res == null) return;
        COSDictionary xobjects = res.getXObjects();
        if (xobjects == null) return;

        // Track CTM through content stream
        double[] ctm = {1, 0, 0, 1, 0, 0};
        Deque<double[]> ctmStack = new ArrayDeque<>();

        OperatorCollection ops = page.getContents();
        for (Operator op : ops) {
            String name = op.getName();
            List<COSBase> operands = op.getOperands();
            switch (name) {
                case "q":
                    ctmStack.push(ctm.clone());
                    break;
                case "Q":
                    if (!ctmStack.isEmpty()) ctm = ctmStack.pop();
                    break;
                case "cm":
                    if (operands.size() >= 6) {
                        double[] m = extractMatrix(operands);
                        ctm = multiplyMatrix(m, ctm);
                    }
                    break;
                case "Do":
                    if (operands.size() >= 1 && operands.get(0) instanceof COSName) {
                        String xName = ((COSName) operands.get(0)).getName();
                        COSBase xobj = xobjects.get(xName);
                        xobj = resolveRef(xobj);
                        if (xobj instanceof COSStream) {
                            COSStream s = (COSStream) xobj;
                            String subtype = s.getNameAsString("Subtype");
                            if ("Image".equals(subtype)) {
                                XImage img = new XImage(s, xName, null);
                                Matrix mat = new Matrix(ctm[0], ctm[1], ctm[2],
                                        ctm[3], ctm[4], ctm[5]);
                                placements.add(new ImagePlacement(img, mat, page));
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns the list of found image placements.
     *
     * @return unmodifiable list of placements
     */
    public List<ImagePlacement> getImagePlacements() {
        return Collections.unmodifiableList(placements);
    }

    /**
     * Clears previously found placements.
     */
    public void reset() {
        placements.clear();
    }

    private static double[] extractMatrix(List<COSBase> operands) {
        double[] m = new double[6];
        for (int i = 0; i < 6 && i < operands.size(); i++) {
            m[i] = getNumber(operands.get(i));
        }
        return m;
    }

    private static double[] multiplyMatrix(double[] m1, double[] m2) {
        return new double[]{
            m1[0] * m2[0] + m1[1] * m2[2],
            m1[0] * m2[1] + m1[1] * m2[3],
            m1[2] * m2[0] + m1[3] * m2[2],
            m1[2] * m2[1] + m1[3] * m2[3],
            m1[4] * m2[0] + m1[5] * m2[2] + m2[4],
            m1[4] * m2[1] + m1[5] * m2[3] + m2[5]
        };
    }

    private static double getNumber(COSBase val) {
        if (val instanceof COSInteger) return ((COSInteger) val).intValue();
        if (val instanceof COSFloat) return ((COSFloat) val).doubleValue();
        return 0;
    }

    private static COSBase resolveRef(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference: " + e.getMessage());
                return null;
            }
        }
        return val;
    }
}
