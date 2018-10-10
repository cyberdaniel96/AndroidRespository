package com.example.johnn.lodgingservicesystemstudent;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

import java.util.HashMap;
import java.util.Map;

public class DOMParserReader {

    private File file;
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private NodeList nodeList;
    public DOMParserReader(String xmlFile) throws Exception{
        file = new File(xmlFile);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.parse(file);
        document.getDocumentElement().normalize();
    }

    public String getNodeName(){
        return document.getDocumentElement().getNodeName();
    }

    public void readElementByTagName(String name){
        nodeList = document.getElementsByTagName(name);
    }

    public Map<String, String> getData(String key, String value) {
        Map<String, String> map = new HashMap<>();

        for (int index = 0; index < nodeList.getLength(); index++) {

            Node node = nodeList.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) node;
                String tag_1 = eElement.getElementsByTagName(key).item(0).getTextContent();
                String tag_2 = eElement.getElementsByTagName(value).item(0).getTextContent();
                System.out.println(tag_1);
                map.put(tag_1, tag_2);

            }
        }
        return map;
    }
//    public static void main(String argv[]) throws Exception{
//        DOMParserReader reader = new DOMParserReader("A:\\FYP Project\\Student Side\\Android\\LodgingServiceSystem(Student)\\app\\src\\main\\java\\data\\MalaysiaState.xsd");
//        //System.out.println(reader.getNodeName());
//        reader.readElementByTagName("state");
//        Map<String, String> map = reader.getData("name", "priority");
//
//
//    }

}
