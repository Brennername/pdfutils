package com.danielremsburg.pdfutils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.List;

public class ExtractX509 {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ExtractX509 <file.pdf>");
            System.exit(0);
        }
        String input = args[0];
        try {
            fromFile(new File(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fromFile(File file) throws IOException {
        PdfReader myreader = new PdfReader(file);
        PdfDocument pdfDocument = new PdfDocument(myreader);


        SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);
        List<String> mysignames = signatureUtil.getSignatureNames();
        mysignames.stream().forEach((str) -> {


            System.out.println("sig: " + str);
            PdfPKCS7 signature1 = signatureUtil.readSignatureData(str);
            boolean genuineAndWasNotModified = false;
            if (signature1 != null) {

                try {
                    genuineAndWasNotModified = signature1.verifySignatureIntegrityAndAuthenticity();
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("genuine: " + genuineAndWasNotModified);
            FileOutputStream mfos = null;
            Certificate certs[] = signature1.getCertificates();
            for (int i = 0; i < certs.length; i++) {
                System.out.println(certs[i]);
                try {
                    mfos = new FileOutputStream(file.getName() + "__" + str + "__" + i + ".x509");
                    mfos.write(certs[i].getEncoded());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }
}