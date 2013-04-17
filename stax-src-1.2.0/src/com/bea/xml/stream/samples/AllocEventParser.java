package com.bea.xml.stream.samples;

import com.bea.xml.stream.StaticAllocator;
import com.javax.xml.namespace.QName;
import com.javax.xml.stream.*;
import com.javax.xml.stream.events.*;
import com.javax.xml.stream.util.*;

import java.io.FileReader;

/**
 * @author Copyright (c) 2002 by BEA Systems. All Rights Reserved.
 */

public class AllocEventParser {
  private static String filename = null;
  
  private static void printUsage() {
    System.out.println("usage: java com.bea.xml.stream.samples.AllocEventParse <xmlfile>");
  }

  public static void main(String[] args) throws Exception {
    try { 
      filename = args[0];
    } catch (ArrayIndexOutOfBoundsException aioobe){
      printUsage();
      System.exit(0);
    }
    System.setProperty("com.javax.xml.stream.XMLInputFactory", 
                       "com.bea.xml.stream.MXParserFactory");
    System.setProperty("com.javax.xml.stream.XMLOutputFactory", 
                       "com.bea.xml.stream.XMLOutputFactoryBase");
    System.setProperty("com.javax.xml.stream.XMLEventFactory",
                       "com.bea.xml.stream.EventFactory");
    

    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                      Boolean.FALSE);

    XMLEventReader r = 
      factory.createXMLEventReader(new FileReader(filename));
    while(r.hasNext()) {
      XMLEvent e = r.nextEvent();
      System.out.println("ID:"+e.hashCode()+"["+e+"]");
    }
  }
}


