package org.aspose.pdf.engine.security.asn1;

import java.util.HashMap;
import java.util.Map;

/**
 * Well-known OID constants for PKCS#7, X.509, and PDF signatures.
 */
public final class OIDs {

    private OIDs() {}

    // PKCS#7 content types
    public static final String DATA = "1.2.840.113549.1.7.1";
    public static final String SIGNED_DATA = "1.2.840.113549.1.7.2";

    // Digest algorithms
    public static final String MD5 = "1.2.840.113549.2.5";
    public static final String SHA1 = "1.3.14.3.2.26";
    public static final String SHA256 = "2.16.840.1.101.3.4.2.1";
    public static final String SHA384 = "2.16.840.1.101.3.4.2.2";
    public static final String SHA512 = "2.16.840.1.101.3.4.2.3";

    // Signature algorithms
    public static final String RSA_ENCRYPTION = "1.2.840.113549.1.1.1";
    public static final String SHA1_WITH_RSA = "1.2.840.113549.1.1.5";
    public static final String SHA256_WITH_RSA = "1.2.840.113549.1.1.11";
    public static final String SHA384_WITH_RSA = "1.2.840.113549.1.1.12";
    public static final String SHA512_WITH_RSA = "1.2.840.113549.1.1.13";

    // Authenticated attributes
    public static final String CONTENT_TYPE = "1.2.840.113549.1.9.3";
    public static final String MESSAGE_DIGEST = "1.2.840.113549.1.9.4";
    public static final String SIGNING_TIME = "1.2.840.113549.1.9.5";

    private static final Map<String, String> OID_TO_JCA_DIGEST = new HashMap<>();
    private static final Map<String, String> OID_TO_JCA_SIG = new HashMap<>();
    private static final Map<String, String> JCA_TO_OID_DIGEST = new HashMap<>();
    private static final Map<String, String> JCA_TO_SIG_OID = new HashMap<>();

    static {
        OID_TO_JCA_DIGEST.put(MD5, "MD5");
        OID_TO_JCA_DIGEST.put(SHA1, "SHA-1");
        OID_TO_JCA_DIGEST.put(SHA256, "SHA-256");
        OID_TO_JCA_DIGEST.put(SHA384, "SHA-384");
        OID_TO_JCA_DIGEST.put(SHA512, "SHA-512");

        // Some PKCS#7 producers (notably older Adobe PDF signers) put the
        // combined "X with RSA" OID in the digestAlgorithm slot of SignerInfo
        // instead of the bare digest OID. Recognise these so dispatch picks
        // the correct hash function.
        OID_TO_JCA_DIGEST.put(SHA1_WITH_RSA, "SHA-1");
        OID_TO_JCA_DIGEST.put(SHA256_WITH_RSA, "SHA-256");
        OID_TO_JCA_DIGEST.put(SHA384_WITH_RSA, "SHA-384");
        OID_TO_JCA_DIGEST.put(SHA512_WITH_RSA, "SHA-512");

        OID_TO_JCA_SIG.put(SHA1_WITH_RSA, "SHA1withRSA");
        OID_TO_JCA_SIG.put(SHA256_WITH_RSA, "SHA256withRSA");
        OID_TO_JCA_SIG.put(SHA384_WITH_RSA, "SHA384withRSA");
        OID_TO_JCA_SIG.put(SHA512_WITH_RSA, "SHA512withRSA");
        OID_TO_JCA_SIG.put(RSA_ENCRYPTION, "RSA");

        JCA_TO_OID_DIGEST.put("SHA-256", SHA256);
        JCA_TO_OID_DIGEST.put("SHA-384", SHA384);
        JCA_TO_OID_DIGEST.put("SHA-512", SHA512);
        JCA_TO_OID_DIGEST.put("SHA-1", SHA1);
        JCA_TO_OID_DIGEST.put("MD5", MD5);

        JCA_TO_SIG_OID.put("SHA256withRSA", SHA256_WITH_RSA);
        JCA_TO_SIG_OID.put("SHA384withRSA", SHA384_WITH_RSA);
        JCA_TO_SIG_OID.put("SHA512withRSA", SHA512_WITH_RSA);
        JCA_TO_SIG_OID.put("SHA1withRSA", SHA1_WITH_RSA);
    }

    /** Maps OID to JCA digest algorithm name. */
    public static String toJCADigest(String oid) {
        return OID_TO_JCA_DIGEST.getOrDefault(oid, "SHA-256");
    }

    /** Maps OID to JCA signature algorithm name. */
    public static String toJCASignature(String oid) {
        return OID_TO_JCA_SIG.getOrDefault(oid, "SHA256withRSA");
    }

    /** Maps JCA digest name to OID. */
    public static String digestToOID(String jcaName) {
        return JCA_TO_OID_DIGEST.getOrDefault(jcaName, SHA256);
    }

    /** Maps JCA signature name to OID. */
    public static String signatureToOID(String jcaName) {
        return JCA_TO_SIG_OID.getOrDefault(jcaName, SHA256_WITH_RSA);
    }

    /** Returns the JCA signature algorithm for a digest + RSA combo. */
    public static String signatureAlgorithmForDigest(String digestAlg) {
        switch (digestAlg) {
            case "SHA-1": return "SHA1withRSA";
            case "SHA-384": return "SHA384withRSA";
            case "SHA-512": return "SHA512withRSA";
            default: return "SHA256withRSA";
        }
    }
}
