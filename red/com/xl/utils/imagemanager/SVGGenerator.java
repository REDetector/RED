package com.xl.utils.imagemanager;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Administrator on 2014/10/11.
 */
public class SVGGenerator {

    public static void writeSVG(PrintWriter pr, Component c) throws IOException {

        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        c.paint(svgGenerator);

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        svgGenerator.stream(pr, false);
    }

}
