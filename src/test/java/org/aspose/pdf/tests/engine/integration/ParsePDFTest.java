package org.aspose.pdf.tests.engine.integration;

import org.aspose.pdf.engine.tools.PDFDocumentAnalyzer;
import org.aspose.pdf.engine.tools.PDFDocumentAnalyzer.ObjectInfo;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ParsePDFTest
{
    @Test
    public void parsePdfTest() throws IOException
    {
        String testRoot = "d:\\Aspose.PDF\\Aspose.PDF\\";
        String fileName = testRoot + "PdfWithText.pdf";

        PDFDocumentAnalyzer analyzer = new PDFDocumentAnalyzer();
        InputStream input = new FileInputStream(fileName);
        analyzer.open(input);

        List<ObjectInfo> objects = analyzer.getObjects();
        for(ObjectInfo info : objects)
        {
            System.out.println(info.objectNumber + ":" + info.generationNumber + " " + info.cosType);
        }

        analyzer.close();
    }

}
