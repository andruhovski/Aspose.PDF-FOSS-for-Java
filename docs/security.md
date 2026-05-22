# Security: Encryption, Permissions, and Signatures

This guide covers password protection, permission flags, and digital signatures.

## Encrypting a PDF

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.CryptoAlgorithm;
import org.aspose.pdf.facades.DocumentPrivilege;

try (Document doc = new Document("plain.pdf")) {
    int permissions = DocumentPrivilege.ALLOW_PRINT
                    | DocumentPrivilege.ALLOW_COPY;

    doc.encrypt(
        "userPwd",        // user password (required to open)
        "ownerPwd",       // owner password (required to change permissions)
        permissions,
        CryptoAlgorithm.AESx256);

    doc.save("encrypted.pdf");
}
```

Algorithms available via `CryptoAlgorithm`:

| Algorithm | Key length | Notes |
|---|---|---|
| `CryptoAlgorithm.RC4x40` | 40-bit | Legacy; **not recommended** — easily broken |
| `CryptoAlgorithm.RC4x128` | 128-bit | Common in older PDFs |
| `CryptoAlgorithm.AESx128` | 128-bit | Reasonable security |
| `CryptoAlgorithm.AESx256` | 256-bit | **Recommended** for new documents |

For modern documents, always use `AESx256` unless you have a specific compatibility requirement.

## Permission flags

Permission flags are bit-mask integers defined as constants in `DocumentPrivilege`:

| Constant | Meaning |
|---|---|
| `ALLOW_PRINT` | Printing allowed |
| `ALLOW_DEGRADED_PRINTING` | Low-res printing allowed |
| `ALLOW_MODIFY_CONTENTS` | Document content can be modified |
| `ALLOW_MODIFY_ANNOTATIONS` | Annotations can be added or modified |
| `ALLOW_COPY` | Text and graphics can be copied/extracted |
| `ALLOW_FILL_IN` | Form fields can be filled in |
| `ALLOW_SCREEN_READERS` | Accessibility tools can extract content |
| `ALLOW_ASSEMBLY` | Pages can be inserted/rotated/deleted |

Combine flags with bitwise OR:

```java
int permissions = DocumentPrivilege.ALLOW_PRINT
                | DocumentPrivilege.ALLOW_COPY
                | DocumentPrivilege.ALLOW_SCREEN_READERS;
```

For "all allowed" or "all forbidden":

```java
DocumentPrivilege all = DocumentPrivilege.getAllowAll();
DocumentPrivilege none = DocumentPrivilege.getForbidAll();
int allValue = all.getValue();
```

Permissions are **advisory** in the PDF spec — they bind only PDF readers that choose to honor them. Treat permissions as a UX hint, not as a security boundary against a determined attacker.

## Reading an encrypted PDF

Pass the password to the `Document` constructor:

```java
try (Document doc = new Document("encrypted.pdf", "userPwd")) {
    // ... normal operations
}
```

If you have only the owner password, use that — it grants full access.

If the password is wrong, the constructor throws an `IOException`. The library does not distinguish "wrong password" from other open errors in the exception type; check the message.

## Removing encryption

To decrypt, open with the password and save without re-encrypting:

```java
try (Document doc = new Document("encrypted.pdf", "userPwd")) {
    doc.save("decrypted.pdf");  // saved without encryption by default
}
```

## Digital signatures

Digital signatures use PKCS#7 (per ISO 32000-1 § 12.8). You need a signing certificate and private key, typically from a PKCS#12 (.p12 / .pfx) file.

```java
// Pseudocode — exact API varies by version; see SignatureField and the
// signature subsystem in src/main/java/org/aspose/pdf/forms/ and
// engine/signature/ for the methods available in your build.
```

Refer to JavaDoc (`mvn javadoc:javadoc`) for `SignatureField` and the signing classes for the current API. Key concepts:

- Signing creates an embedded PKCS#7 blob covering a specified byte range of the PDF.
- Verification checks the digest and validates the certificate chain.
- Multiple signatures can co-exist on one document; each covers an incremental update.

A complete signing example will be added to the docs as the API surface stabilises in beta.

## Verifying a signature

```java
try (Document doc = new Document("signed.pdf")) {
    for (Field field : doc.getForm()) {
        if (field instanceof SignatureField) {
            SignatureField sig = (SignatureField) field;
            // ... query sig for validity, signer name, time, etc.
        }
    }
}
```

## Best practices

- **Use AES-256.** Older algorithms have known weaknesses. RC4 is broken; AES-128 is still solid but offers less margin than AES-256.
- **Use long, random owner passwords.** The owner password is what actually defends the file. The user password is for "softer" access control.
- **Don't rely on permission flags alone.** PDF permissions are honored by cooperative readers and can be ignored by determined attackers. If content must be confidential, encrypt it; if it must not be copied, don't include it in the PDF.
- **Don't reuse keys across documents.** Each document should be encrypted with its own random password / key.
- **Be careful with passwords in logs.** Don't accidentally write the user/owner password into application logs or error messages.

## Known limitations

- **Public-key encryption (certificate-based)** is supported via the `ICustomSecurityHandler` interface but the high-level convenience API may differ from the commercial product.
- **Encryption-related metadata** (filter chains, custom CryptFilter dictionaries) follows the spec; non-standard producer extensions may not be recognized.
- **Signature long-term validation (LTV)** features (DSS dictionary, document timestamps) are partially implemented; check the JavaDoc and your tests for current status.
