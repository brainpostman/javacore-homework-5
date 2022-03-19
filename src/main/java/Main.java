import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = new String[]{"id", "firstName", "lastName", "country", "age"};
        String fileNameCSV = "data.csv";
        String fileNameXML = "data.xml";
        List<Employee> list = parseCSV(columnMapping, fileNameCSV);
        List<Employee> list2 = parseXML(fileNameXML);
        String json = listToJson(list);
        String json2 = listToJson(list2);
        String jsonFile = "data.json";
        String jsonFile2 = "data2.json";
        writeString(json, jsonFile);
        writeString(json2, jsonFile2);
        String jsonString = readString(jsonFile);
        List<Employee> list3 = jsonToList(jsonString);
        for (Employee emp : list3) {
            System.out.println(emp);
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = null;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            list = csv.parse();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public static List<Employee> parseXML(String fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        List<Employee> employees = new ArrayList<>();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fileName);
            Node staff = doc.getDocumentElement();
            NodeList staffNodeList = staff.getChildNodes();
            for (int i = 0; i < staffNodeList.getLength(); i++) {
                Node nodeEmployee = staffNodeList.item(i);
                if (Node.ELEMENT_NODE == nodeEmployee.getNodeType()) {
                    Element element = (Element) nodeEmployee;
                    long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());
                    employees.add(new Employee(id, firstName, lastName, country, age));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace();
        }
        return employees;
    }

    public static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(list, listType);
    }

    public static void writeString(String file, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(file);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readString(String fileName) {
        String json = null;
        try (BufferedReader buffer = new BufferedReader(new FileReader(fileName))) {
            json = buffer.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    public static List<Employee> jsonToList(String jsonString) {
        {
            JSONParser parser = new JSONParser();
            List<Employee> employees = new ArrayList<>();
            try {
                JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                for (Object o : jsonArray) {
                    String empString = o.toString();
                    Employee emp = gson.fromJson(empString, Employee.class);
                    employees.add(emp);
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            return employees;
        }
    }
}
