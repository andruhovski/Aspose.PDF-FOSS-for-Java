package org.aspose.pdf.engine.security.signature;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
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
        COSDictionary sigDict = buildSignatureDictionary(
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

    private COSDictionary buildSignatureDictionary(X509Certificate certificate,
                                                    String reason, String contact,
                                                    String location, String subFilter) {
        COSDictionary sigDict = new COSDictionary();
        sigDict.set(COSName.of("Type"), COSName.of("Sig"));
        sigDict.set(COSName.of("Filter"), COSName.of("Adobe.PPKLite"));
        sigDict.set(COSName.of("SubFilter"), COSName.of(subFilter));

        if (reason != null) {
            sigDict.set(COSName.of("Reason"),
                    new COSString(reason.getBytes(StandardCharsets.UTF_8)));
        }
        if (contact != null) {
            sigDict.set(COSName.of("ContactInfo"),
                    new COSString(contact.getBytes(StandardCharsets.UTF_8)));
        }
        if (location != null) {
            sigDict.set(COSName.of("Location"),
                    new COSString(location.getBytes(StandardCharsets.UTF_8)));
        }
        String signerName = certificate.getSubjectX500Principal().getName();
        sigDict.set(COSName.of("Name"),
                new COSString(signerName.getBytes(StandardCharsets.UTF_8)));

        SimpleDateFormat sdf = new SimpleDateFormat("'D:'yyyyMMddHHmmssZ");
        sigDict.set(COSName.of("M"),
                new COSString(sdf.format(new Date()).getBytes(StandardCharsets.UTF_8)));

        // ByteRange placeholder — will be overwritten after serialization
        // Use large placeholder values to ensure enough space for actual offsets
        COSArray byteRange = new COSArray();
        byteRange.add(COSInteger.valueOf(0));
        byteRange.add(COSInteger.valueOf(9999999999L));
        byteRange.add(COSInteger.valueOf(9999999999L));
        byteRange.add(COSInteger.valueOf(9999999999L));
        sigDict.set(COSName.of("ByteRange"), byteRange);

        // Contents placeholder (hex string of zeros)
        byte[] placeholder = new byte[SIGNATURE_SIZE];
        COSString contentsStr = new COSString(placeholder);
        contentsStr.setForceHex(true);
        sigDict.set(COSName.of("Contents"), contentsStr);

        return sigDict;
    }

    private void attachSignatureToField(Document document, String sigFieldName,
                                         COSDictionary sigDict) throws IOException {
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
            COSDictionary fieldDict = new COSDictionary();
            fieldDict.set(COSName.of("FT"), COSName.of("Sig"));
            fieldDict.set(COSName.of("T"),
                    new COSString(name.getBytes(StandardCharsets.UTF_8)));
            // Invisible annotation rect
            COSArray rect = new COSArray();
            rect.add(COSInteger.valueOf(0)); rect.add(COSInteger.valueOf(0));
            rect.add(COSInteger.valueOf(0)); rect.add(COSInteger.valueOf(0));
            fieldDict.set(COSName.of("Rect"), rect);
            fieldDict.set(COSName.of("Type"), COSName.of("Annot"));
            fieldDict.set(COSName.of("Subtype"), COSName.of("Widget"));

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
        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        int maxObjNum = 0;
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj = parser.getObject(key);
            if (obj != null && !(obj instanceof COSNull)) {
                objects.put(key, obj);
                maxObjNum = Math.max(maxObjNum, key.getObjectNumber());
            }
        }

        // Get AcroForm — create if needed
        COSDictionary catalog = parser.getCatalog();
        COSBase acroFormRef = catalog.get(COSName.of("AcroForm"));
        COSDictionary acroFormDict;
        COSObjectKey acroFormKey = null;

        if (acroFormRef != null) {
            COSBase resolved = parser.resolveReference(acroFormRef);
            acroFormDict = (COSDictionary) resolved;
            // Find its key
            if (acroFormRef instanceof COSObjectReference) {
                acroFormKey = ((COSObjectReference) acroFormRef).getKey();
            }
        } else {
            // Create new AcroForm dict
            acroFormDict = new COSDictionary();
            acroFormDict.set(COSName.of("Fields"), new COSArray());
            acroFormKey = new COSObjectKey(++maxObjNum, 0);
            objects.put(acroFormKey, acroFormDict);
            catalog.set(COSName.of("AcroForm"),
                    new COSObjectReference(acroFormKey, k -> objects.get(k)));
        }

        // Add any form fields that don't have object keys yet
        Form form = document.getForm();
        COSBase fieldsRef = acroFormDict.get("Fields");
        COSArray fieldsArray;
        if (fieldsRef instanceof COSArray) {
            fieldsArray = (COSArray) fieldsRef;
        } else {
            fieldsArray = new COSArray();
            acroFormDict.set(COSName.of("Fields"), fieldsArray);
        }

        for (Field field : form) {
            COSDictionary fieldDict = field.getCOSDictionary();
            COSObjectKey fieldKey = fieldDict.getObjectKey();
            // Identity check: the field may carry a pre-assigned key from
            // Document.registerImportedObject that collides with a key we
            // just allocated for a new acroFormDict. Treat any mismatch as
            // unregistered and allocate a fresh key.
            if (fieldKey == null || objects.get(fieldKey) != fieldDict) {
                fieldKey = new COSObjectKey(++maxObjNum, 0);
                fieldDict.setObjectKey(fieldKey);
            }
            objects.put(fieldKey, fieldDict);

            // Ensure the field is referenced from the AcroForm /Fields array.
            boolean found = false;
            for (int i = 0; i < fieldsArray.size(); i++) {
                COSBase item = fieldsArray.get(i);
                if (item == fieldDict) { found = true; break; }
                if (item instanceof COSObjectReference) {
                    COSObjectKey rk = ((COSObjectReference) item).getKey();
                    if (rk.equals(fieldKey)) { found = true; break; }
                }
            }
            if (!found) {
                final COSObjectKey refKey = fieldKey;
                fieldsArray.add(new COSObjectReference(refKey, k -> objects.get(k)));
            }

            // Also register /V (signature dict) if it's not tracked
            COSBase vRef = fieldDict.get("V");
            if (vRef instanceof COSDictionary) {
                COSDictionary vDict = (COSDictionary) vRef;
                COSObjectKey vKey = vDict.getObjectKey();
                if (vKey == null || objects.get(vKey) != vDict) {
                    vKey = new COSObjectKey(++maxObjNum, 0);
                    vDict.setObjectKey(vKey);
                }
                objects.put(vKey, vDict);
                // Replace inline dict with indirect reference
                final COSObjectKey vRefKey = vKey;
                fieldDict.set(COSName.of("V"),
                        new COSObjectReference(vRefKey, k -> objects.get(k)));
            }
        }

        // Update AcroForm in objects map if it has a key
        if (acroFormKey != null) {
            objects.put(acroFormKey, acroFormDict);
        }

        // Write
        COSDictionary trailer = parser.getTrailer();
        trailer.set(COSName.of("Size"), COSInteger.valueOf(maxObjNum + 1));
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
