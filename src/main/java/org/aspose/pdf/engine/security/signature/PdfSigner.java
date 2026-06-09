package org.aspose.pdf.engine.security.signature;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.security.pkcs7.PKCS7SignedData;
import org.aspose.pdf.engine.writer.PDFWriter;
import org.aspose.pdf.forms.Field;
import org.aspose.pdf.forms.Form;
import org.aspose.pdf.forms.Signature;
import org.aspose.pdf.forms.SignatureField;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Signs and verifies PDF documents using PKCS#7 detached signatures
 * (ISO 32000-1:2008, §12.8).
 * <p>
 * The signing process:
 * <ol>
 *   <li>Build a signature dictionary with placeholder /ByteRange and /Contents</li>
 *   <li>Attach it to a SignatureField's /V entry</li>
 *   <li>Serialize the document to bytes</li>
 *   <li>Locate the /Contents hex placeholder and compute actual byte ranges</li>
 *   <li>Extract signed bytes, create PKCS#7, embed into /Contents</li>
 *   <li>Update /ByteRange with actual values</li>
 * </ol>
 * </p>
 */
public class PdfSigner {

    private static final Logger LOG = Logger.getLogger(PdfSigner.class.getName());
    private static final int SIGNATURE_SIZE = 8192; // bytes for PKCS#7 container

    /**
     * Signs a PDF document using raw key material.
     *
     * @param document        the document to sign
     * @param sigFieldName    signature field name (null to create new "Signature1")
     * @param privateKey      the signing private key
     * @param certificate     the signer's certificate
     * @param chain           certificate chain (may be null)
     * @param digestAlgorithm "SHA-256", "SHA-1", etc.
     * @param reason          signing reason (may be null)
     * @param contact         signer contact info (may be null)
     * @param location        signing location (may be null)
     * @param output          output stream for signed PDF
     * @throws Exception if signing fails
     */
    public void sign(Document document, String sigFieldName, PrivateKey privateKey,
                     X509Certificate certificate, X509Certificate[] chain,
                     String digestAlgorithm, String reason, String contact,
                     String location, OutputStream output) throws Exception {

        // Step 1: Build signature dictionary
        PdfDictionary sigDict = buildSignatureDictionary(
                certificate, reason, contact, location, "adbe.pkcs7.detached");

        // Step 2: Attach to signature field
        attachSignatureToField(document, sigFieldName, sigDict);

        // Step 3: Serialize, compute, sign, embed
        byte[] signedPdf = serializeAndSign(document, privateKey, certificate, chain, digestAlgorithm);

        output.write(signedPdf);
    }

    /**
     * Signs a PDF document using a {@link Signature} object (public API).
     *
     * @param document     the document to sign
     * @param sigFieldName signature field name (null to create new)
     * @param signature    the signature object containing key material and metadata
     * @param output       output stream for signed PDF
     * @throws Exception if signing fails
     */
    public void sign(Document document, String sigFieldName, Signature signature,
                     OutputStream output) throws Exception {
        sign(document, sigFieldName,
                signature.getPrivateKey(),
                signature.getCertificate(),
                signature.getCertificateChain(),
                "SHA-256",
                signature.getReason(),
                signature.getContactInfo(),
                signature.getLocation(),
                output);
    }

    /**
     * Verifies all signatures in a PDF document.
     *
     * @param document the document to verify
     * @return list of verification results
     * @throws Exception if verification fails
     */
    public List<SignatureVerificationResult> verify(Document document) throws Exception {
        List<SignatureVerificationResult> results = new ArrayList<>();
        Form form = document.getForm();
        for (Field field : form) {
            if (field instanceof SignatureField) {
                SignatureField sigField = (SignatureField) field;
                if (sigField.isSigned()) {
                    results.add(verifySignature(sigField, document));
                }
            }
        }
        return results;
    }

    /**
     * Verifies all signatures in a PDF loaded from raw bytes, using byte-range integrity.
     *
     * @param pdfBytes the raw PDF bytes
     * @return list of verification results
     * @throws Exception if verification fails
     */
    public List<SignatureVerificationResult> verify(byte[] pdfBytes) throws Exception {
        Document document = new Document(new ByteArrayInputStream(pdfBytes));
        List<SignatureVerificationResult> results = new ArrayList<>();
        Form form = document.getForm();
        for (Field field : form) {
            if (field instanceof SignatureField) {
                SignatureField sigField = (SignatureField) field;
                if (sigField.isSigned()) {
                    results.add(verifySignatureWithBytes(sigField, pdfBytes));
                }
            }
        }
        document.close();
        return results;
    }

    // ── Private helpers ──

    private PdfDictionary buildSignatureDictionary(X509Certificate certificate,
                                                    String reason, String contact,
                                                    String location, String subFilter) {
        PdfDictionary sigDict = new PdfDictionary();
        sigDict.set(PdfName.of("Type"), PdfName.of("Sig"));
        sigDict.set(PdfName.of("Filter"), PdfName.of("Adobe.PPKLite"));
        sigDict.set(PdfName.of("SubFilter"), PdfName.of(subFilter));

        if (reason != null) {
            sigDict.set(PdfName.of("Reason"),
                    new PdfString(reason.getBytes(StandardCharsets.UTF_8)));
        }
        if (contact != null) {
            sigDict.set(PdfName.of("ContactInfo"),
                    new PdfString(contact.getBytes(StandardCharsets.UTF_8)));
        }
        if (location != null) {
            sigDict.set(PdfName.of("Location"),
                    new PdfString(location.getBytes(StandardCharsets.UTF_8)));
        }
        String signerName = certificate.getSubjectX500Principal().getName();
        sigDict.set(PdfName.of("Name"),
                new PdfString(signerName.getBytes(StandardCharsets.UTF_8)));

        SimpleDateFormat sdf = new SimpleDateFormat("'D:'yyyyMMddHHmmssZ");
        sigDict.set(PdfName.of("M"),
                new PdfString(sdf.format(new Date()).getBytes(StandardCharsets.UTF_8)));

        // ByteRange placeholder — will be overwritten after serialization
        // Use large placeholder values to ensure enough space for actual offsets
        PdfArray byteRange = new PdfArray();
        byteRange.add(PdfInteger.valueOf(0));
        byteRange.add(PdfInteger.valueOf(9999999999L));
        byteRange.add(PdfInteger.valueOf(9999999999L));
        byteRange.add(PdfInteger.valueOf(9999999999L));
        sigDict.set(PdfName.of("ByteRange"), byteRange);

        // Contents placeholder (hex string of zeros)
        byte[] placeholder = new byte[SIGNATURE_SIZE];
        PdfString contentsStr = new PdfString(placeholder);
        contentsStr.setForceHex(true);
        sigDict.set(PdfName.of("Contents"), contentsStr);

        return sigDict;
    }

    private void attachSignatureToField(Document document, String sigFieldName,
                                         PdfDictionary sigDict) throws IOException {
        Form form = document.getForm();
        SignatureField sigField = null;

        // Try to find existing signature field
        if (sigFieldName != null) {
            for (Field field : form) {
                if (field instanceof SignatureField
                        && sigFieldName.equals(field.getFullName())) {
                    sigField = (SignatureField) field;
                    break;
                }
            }
        }

        if (sigField == null) {
            // Create new signature field
            String name = sigFieldName != null ? sigFieldName : "Signature1";
            PdfDictionary fieldDict = new PdfDictionary();
            fieldDict.set(PdfName.of("FT"), PdfName.of("Sig"));
            fieldDict.set(PdfName.of("T"),
                    new PdfString(name.getBytes(StandardCharsets.UTF_8)));
            // Invisible annotation rect
            PdfArray rect = new PdfArray();
            rect.add(PdfInteger.valueOf(0)); rect.add(PdfInteger.valueOf(0));
            rect.add(PdfInteger.valueOf(0)); rect.add(PdfInteger.valueOf(0));
            fieldDict.set(PdfName.of("Rect"), rect);
            fieldDict.set(PdfName.of("Type"), PdfName.of("Annot"));
            fieldDict.set(PdfName.of("Subtype"), PdfName.of("Widget"));

            sigField = new SignatureField(fieldDict, null, name);
            form.add(sigField);
        }

        // Set the signature dictionary as /V
        sigField.setSignatureDictionary(sigDict);
    }

    private byte[] serializeAndSign(Document document, PrivateKey privateKey,
                                     X509Certificate certificate, X509Certificate[] chain,
                                     String digestAlgorithm) throws Exception {
        // Collect all objects from the parser and add any new objects
        // (signature field, signature dict) that aren't in the parser's object table
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        serializeDocumentWithNewObjects(document, pdfOut);
        byte[] pdfBytes = pdfOut.toByteArray();

        // Find /Contents hex placeholder
        int contentsHexStart = PdfSignatureEmbedder.findContentsHexStart(pdfBytes);
        if (contentsHexStart < 0) {
            throw new IOException("Cannot find /Contents hex placeholder in serialized PDF");
        }
        int contentsHexEnd = PdfSignatureEmbedder.findContentsHexEnd(pdfBytes, contentsHexStart);
        if (contentsHexEnd < 0) {
            throw new IOException("Cannot find end of /Contents hex string");
        }
        int hexSize = contentsHexEnd - contentsHexStart;

        // Compute byte range
        int[] byteRange = PdfSignatureEmbedder.computeByteRange(
                pdfBytes, contentsHexStart, contentsHexEnd);

        // Update ByteRange in PDF before computing digest
        PdfSignatureEmbedder.updateByteRange(pdfBytes, byteRange);

        // Extract signed bytes
        byte[] signedBytes = PdfSignatureEmbedder.extractSignedBytes(pdfBytes, byteRange);

        // Create PKCS#7 signature
        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, chain, digestAlgorithm, signedBytes);
        byte[] pkcs7Bytes = pkcs7.encode();

        // Embed signature into /Contents
        PdfSignatureEmbedder.embedSignature(pdfBytes, contentsHexStart, hexSize, pkcs7Bytes);

        return pdfBytes;
    }

    /**
     * Serializes a document including any new objects added to the model
     * (such as signature fields and signature dictionaries).
     */
    private void serializeDocumentWithNewObjects(Document document,
                                                  OutputStream output) throws IOException {
        PDFParser parser = document.getParser();
        if (parser == null) {
            // New empty document — just use normal save
            document.save(output);
            return;
        }

        // Collect all existing objects from parser
        Map<PdfObjectKey, PdfBase> objects = new LinkedHashMap<>();
        int maxObjNum = 0;
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj = parser.getObject(key);
            if (obj != null && !(obj instanceof PdfNull)) {
                objects.put(key, obj);
                maxObjNum = Math.max(maxObjNum, key.getObjectNumber());
            }
        }

        // Get AcroForm — create if needed
        PdfDictionary catalog = parser.getCatalog();
        PdfBase acroFormRef = catalog.get(PdfName.of("AcroForm"));
        PdfDictionary acroFormDict;
        PdfObjectKey acroFormKey = null;

        if (acroFormRef != null) {
            PdfBase resolved = parser.resolveReference(acroFormRef);
            acroFormDict = (PdfDictionary) resolved;
            // Find its key
            if (acroFormRef instanceof PdfObjectReference) {
                acroFormKey = ((PdfObjectReference) acroFormRef).getKey();
            }
        } else {
            // Create new AcroForm dict
            acroFormDict = new PdfDictionary();
            acroFormDict.set(PdfName.of("Fields"), new PdfArray());
            acroFormKey = new PdfObjectKey(++maxObjNum, 0);
            objects.put(acroFormKey, acroFormDict);
            catalog.set(PdfName.of("AcroForm"),
                    new PdfObjectReference(acroFormKey, k -> objects.get(k)));
        }

        // Add any form fields that don't have object keys yet
        Form form = document.getForm();
        PdfBase fieldsRef = acroFormDict.get("Fields");
        PdfArray fieldsArray;
        if (fieldsRef instanceof PdfArray) {
            fieldsArray = (PdfArray) fieldsRef;
        } else {
            fieldsArray = new PdfArray();
            acroFormDict.set(PdfName.of("Fields"), fieldsArray);
        }

        for (Field field : form) {
            PdfDictionary fieldDict = field.getPdfDictionary();
            PdfObjectKey fieldKey = fieldDict.getObjectKey();
            // Identity check: the field may carry a pre-assigned key from
            // Document.registerImportedObject that collides with a key we
            // just allocated for a new acroFormDict. Treat any mismatch as
            // unregistered and allocate a fresh key.
            if (fieldKey == null || objects.get(fieldKey) != fieldDict) {
                fieldKey = new PdfObjectKey(++maxObjNum, 0);
                fieldDict.setObjectKey(fieldKey);
            }
            objects.put(fieldKey, fieldDict);

            // Ensure the field is referenced from the AcroForm /Fields array.
            boolean found = false;
            for (int i = 0; i < fieldsArray.size(); i++) {
                PdfBase item = fieldsArray.get(i);
                if (item == fieldDict) { found = true; break; }
                if (item instanceof PdfObjectReference) {
                    PdfObjectKey rk = ((PdfObjectReference) item).getKey();
                    if (rk.equals(fieldKey)) { found = true; break; }
                }
            }
            if (!found) {
                final PdfObjectKey refKey = fieldKey;
                fieldsArray.add(new PdfObjectReference(refKey, k -> objects.get(k)));
            }

            // Also register /V (signature dict) if it's not tracked
            PdfBase vRef = fieldDict.get("V");
            if (vRef instanceof PdfDictionary) {
                PdfDictionary vDict = (PdfDictionary) vRef;
                PdfObjectKey vKey = vDict.getObjectKey();
                if (vKey == null || objects.get(vKey) != vDict) {
                    vKey = new PdfObjectKey(++maxObjNum, 0);
                    vDict.setObjectKey(vKey);
                }
                objects.put(vKey, vDict);
                // Replace inline dict with indirect reference
                final PdfObjectKey vRefKey = vKey;
                fieldDict.set(PdfName.of("V"),
                        new PdfObjectReference(vRefKey, k -> objects.get(k)));
            }
        }

        // Update AcroForm in objects map if it has a key
        if (acroFormKey != null) {
            objects.put(acroFormKey, acroFormDict);
        }

        // Write
        PdfDictionary trailer = parser.getTrailer();
        trailer.set(PdfName.of("Size"), PdfInteger.valueOf(maxObjNum + 1));
        PDFWriter writer = new PDFWriter(output, parser.getVersion());
        writer.write(trailer, objects);
    }

    private SignatureVerificationResult verifySignature(SignatureField field, Document doc) {
        try {
            byte[] pkcs7Bytes = field.getSignatureBytes();
            if (pkcs7Bytes == null || pkcs7Bytes.length == 0) {
                return new SignatureVerificationResult(
                        field.getFullName(), false, null, null, null,
                        field.getReason(), field.getLocation());
            }

            byte[] trimmed = trimTrailingZeros(pkcs7Bytes);
            PKCS7SignedData pkcs7 = PKCS7SignedData.parse(trimmed);
            List<X509Certificate> certs = pkcs7.getCertificates();
            X509Certificate cert = certs.isEmpty() ? null : certs.get(0);
            String signerName = cert != null
                    ? cert.getSubjectX500Principal().getName()
                    : field.getSignerName();
            Date signingTime = null;
            if (!pkcs7.getSignerInfos().isEmpty()) {
                signingTime = pkcs7.getSignerInfos().get(0).getSigningTime();
            }

            // Structural validity check (without raw PDF bytes we can't do byte-range verification)
            boolean valid = !pkcs7.getSignerInfos().isEmpty();

            return new SignatureVerificationResult(
                    field.getFullName(), valid, signerName, signingTime, cert,
                    field.getReason(), field.getLocation());
        } catch (Exception e) {
            LOG.fine(() -> "Signature verification failed: " + e.getMessage());
            return new SignatureVerificationResult(
                    field.getFullName(), false, null, null, null,
                    field.getReason(), field.getLocation());
        }
    }

    private SignatureVerificationResult verifySignatureWithBytes(SignatureField field,
                                                                  byte[] pdfBytes) {
        try {
            byte[] pkcs7Bytes = field.getSignatureBytes();
            if (pkcs7Bytes == null || pkcs7Bytes.length == 0) {
                return new SignatureVerificationResult(
                        field.getFullName(), false, null, null, null,
                        field.getReason(), field.getLocation());
            }

            byte[] trimmed = trimTrailingZeros(pkcs7Bytes);
            PKCS7SignedData pkcs7 = PKCS7SignedData.parse(trimmed);
            List<X509Certificate> certs = pkcs7.getCertificates();
            X509Certificate cert = certs.isEmpty() ? null : certs.get(0);
            String signerName = cert != null
                    ? cert.getSubjectX500Principal().getName()
                    : field.getSignerName();
            Date signingTime = null;
            if (!pkcs7.getSignerInfos().isEmpty()) {
                signingTime = pkcs7.getSignerInfos().get(0).getSigningTime();
            }

            // Full byte-range verification
            int[] byteRange = field.getByteRange();
            boolean valid = false;
            if (byteRange != null && byteRange.length == 4) {
                byte[] signedBytes = PdfSignatureEmbedder.extractSignedBytes(pdfBytes, byteRange);
                valid = pkcs7.verify(signedBytes);
            }

            return new SignatureVerificationResult(
                    field.getFullName(), valid, signerName, signingTime, cert,
                    field.getReason(), field.getLocation());
        } catch (Exception e) {
            LOG.fine(() -> "Signature verification failed: " + e.getMessage());
            return new SignatureVerificationResult(
                    field.getFullName(), false, null, null, null,
                    field.getReason(), field.getLocation());
        }
    }

    private static byte[] trimTrailingZeros(byte[] data) {
        int len = data.length;
        while (len > 0 && data[len - 1] == 0) len--;
        return Arrays.copyOf(data, len);
    }
}
