package by.bsuir.parser;

import by.bsuir.bean.characters.Client;
import by.bsuir.bean.space.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Client xml parser.
 */
public class ClientXmlParser implements XmlParser<Client> {
    private static final String CLIENT = "client";
    private static final String CLIENTS = "clients";
    private static final String ID = "id";
    private static final String FIRST_NAME = "first-name";
    private static final String LAST_NAME = "last-name";
    private static final String MONEY = "money";
    private static final String TABLE = "table";
    private static final String TABLE_NUMBER = "number";
    private static final String TABLE_IS_FREE = "is-free";

    private DocumentBuilder documentBuilder;
    private String sourceFilePath;
    private String xsdFilePath;

    /**
     * Gets source file path.
     *
     * @return the source file path
     */
    public String getSourceFilePath() {
        return sourceFilePath;
    }

    /**
     * Gets xsd file path.
     *
     * @return the xsd file path
     */
    public String getXsdFilePath() {
        return xsdFilePath;
    }

    /**
     * Instantiates a new Client xml parser.
     */
    public ClientXmlParser() {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Instantiates a new Client xml parser.
     *
     * @param sourceFilePath the source file path
     * @param xsdFilePath    the xsd file path
     */
    public ClientXmlParser(String sourceFilePath, String xsdFilePath) {
        this();
        this.sourceFilePath = sourceFilePath;
        this.xsdFilePath = xsdFilePath;
    }

    @Override
    public List<Client> getData() throws XmlParserException {
        var sourceFile = new File(sourceFilePath);
        var xsdFile = new File(xsdFilePath);

        if (!sourceFile.exists()) {
            throw new XmlParserException(sourceFilePath + ": file not exists.");
        }

        if (!xsdFile.exists()) {
            throw new XmlParserException(sourceFilePath + ": file not exists.");
        }

        validateXMLByXSD(sourceFile, xsdFile);

        List<Client> clients = new ArrayList<>();
        Document document;

        try {
            document = documentBuilder.parse(sourceFile);
        } catch (SAXException | IOException e) {
            throw new XmlParserException(e.getMessage());
        }

        var element = document.getDocumentElement();
        var nodeClients = element.getElementsByTagName(CLIENT);

        for (var i = 0; i < nodeClients.getLength(); i++) {
            if (nodeClients.item(i).getNodeType() == Node.ELEMENT_NODE) {
                clients.add(getClientElement((Element) nodeClients.item(i)));
            }
        }

        return clients;
    }

    @Override
    public void setData(List<Client> clients) throws XmlParserException {
        var document = documentBuilder.newDocument();
        var root = document.createElement(CLIENTS);
        document.appendChild(root);

        for (var client : clients) {
            root.appendChild(getClientElement(document, client));
        }

        var impl = document.getImplementation();
        var implLs = (DOMImplementationLS) impl.getFeature("LS", "3.0");
        var lsSerializer = implLs.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("format-pretty-print", true);
        var output = implLs.createLSOutput();
        output.setEncoding("UTF-8");

        try {
            output.setByteStream(Files.newOutputStream(Paths.get(sourceFilePath)));
        } catch (IOException e) {
            throw new XmlParserException(e.getMessage());
        }

        lsSerializer.write(document, output);
    }

    private Client getClientElement(Element element) {
        Client client;

        try {
            client = new Client(Integer.parseInt(getElementTextContent(element, ID)));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("'" + ID + "'" + "incorrect");
        }

        try {
            client.setMoney(Integer.parseInt(getElementTextContent(element, MONEY)));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("'" + MONEY + "'" + "incorrect");
        }

        client.setFirstName(getElementTextContent(element, FIRST_NAME));
        client.setLastName(getElementTextContent(element, LAST_NAME));

        var tableElement = (Element) element.getElementsByTagName(TABLE).item(0);
        if (tableElement != null) {
            client.setTable(getTableElement(tableElement));
        }

        return client;
    }

    private static String getElementTextContent(Element element, String elementName) {
        var nList = element.getElementsByTagName(elementName);
        var node = nList.item(0);
        return node.getTextContent();
    }

    private Table getTableElement(Element element) {
        var table = new Table(Integer.parseInt(getElementTextContent(element, ID)));

        try {
            table.setFree(Boolean.parseBoolean(getElementTextContent(element, TABLE_IS_FREE)));
        } catch (Exception ex) {
            throw new IllegalArgumentException("'" + TABLE_IS_FREE + "'" + "item incorrect.");
        }

        try {
            table.setNumber(Integer.parseInt(getElementTextContent(element, TABLE_NUMBER)));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("'" + TABLE_NUMBER + "'" + "incorrect");
        }

        return table;
    }

    private Element getClientElement(Document document, Client client) {
        var clientElement = document.createElement(CLIENT);

        var idElement = document.createElement(ID);
        idElement.appendChild(document.createTextNode(Integer.toString(client.getId())));

        var firstNameElement = document.createElement(FIRST_NAME);
        firstNameElement.appendChild(document.createTextNode(client.getFirstName()));

        var lastNameElement = document.createElement(LAST_NAME);
        lastNameElement.appendChild(document.createTextNode(client.getLastName()));

        var moneyElement = document.createElement(MONEY);
        moneyElement.appendChild(document.createTextNode(Double.toString(client.getMoney())));


        clientElement.appendChild(idElement);
        clientElement.appendChild(firstNameElement);
        clientElement.appendChild(lastNameElement);
        clientElement.appendChild(moneyElement);

        if (client.getTable() != null) {
            clientElement.appendChild(getTableElement(document, client.getTable()));
        }

        return clientElement;
    }

    private Element getTableElement(Document document, Table table) {
        var tableElement = document.createElement(TABLE);

        var numberElement = document.createElement(TABLE_NUMBER);
        numberElement.appendChild(document.createTextNode(Integer.toString(table.getNumber())));

        var isFreeElement = document.createElement(TABLE_IS_FREE);
        isFreeElement.appendChild(document.createTextNode(Boolean.toString(table.isFree())));

        tableElement.appendChild(numberElement);
        tableElement.appendChild(isFreeElement);

        return tableElement;
    }

    private void validateXMLByXSD(File xml, File xsd) throws XmlParserException {
        try {
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(xsd)
                    .newValidator()
                    .validate(new StreamSource(xml));
        } catch (Exception e) {
            throw new XmlParserException("Invalid xml format");
        }
    }
}
