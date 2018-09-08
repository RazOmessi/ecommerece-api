package com.openu.apis.configurations;

import com.openu.apis.beans.LookupTableBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {
    private static ConfigurationManager _instance;

    private Map<String, LookupTableBean> lookupsConfigsMap;

    public LookupTableBean getTableConfigs(String lookupName){
        return lookupsConfigsMap.get(lookupName);
    }

    public static ConfigurationManager getInstance() {
        if (_instance != null) {
            return _instance;
        }

        synchronized (ConfigurationManager.class) {
            if (_instance == null) {
                _instance = new ConfigurationManager();
            }

            return _instance;
        }
    }

    private ConfigurationManager() {
        lookupsConfigsMap = new HashMap<>();

        String filename = System.getProperty("ecommeerce.configurations");
        if (filename == null) {
            throw new RuntimeException("missing lookups file configuration. use =Decommeerce.configurations to config path.");
        } else {
            try {
                File xmlFile = new File(filename);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);

                NodeList nList = doc.getElementsByTagName("lookup");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Element.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String name = element.getAttribute("name");
                        String schemaName = element.getElementsByTagName("schemaName").item(0).getTextContent();
                        String tableName = element.getElementsByTagName("tableName").item(0).getTextContent();
                        String keyColumn = element.getElementsByTagName("keyColumn").item(0).getTextContent();
                        String valueColumn = element.getElementsByTagName("valueColumn").item(0).getTextContent();
                        Integer maxCacheSize = Integer.valueOf(element.getElementsByTagName("maxCacheSize").item(0).getTextContent());
                        lookupsConfigsMap.put(name, new LookupTableBean(schemaName, tableName, keyColumn, valueColumn, maxCacheSize));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format("bad lookups file: %s", e.getMessage()));
            }
        }
    }

}
