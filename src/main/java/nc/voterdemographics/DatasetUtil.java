package nc.voterdemographics;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DatasetUtil {

    public String DELIMITER = "\t";
    public boolean validEntry = true;

    // HashMaps for Attribute names and index mapping
    public HashMap<String, String> columnMappings = new LinkedHashMap<>();
    public HashMap<String, Integer> indexMappings = new LinkedHashMap<>();

    public BufferedReader in;
    public BufferedWriter outTraining, outDataset;

    /**
     * Initialize I/O objects and attributes.
     * @throws IOException when file cannot be opened
     */
    public void init() throws IOException {
        in = new BufferedReader(new FileReader("src/main/resources/cumulative_2006-2019.txt"));
        outTraining = new BufferedWriter(new PrintWriter("src/main/resources/TrainingData.csv"));
        outDataset = new BufferedWriter(new PrintWriter("src/main/resources/Dataset.csv"));

        columnMappings.put("age", "Age");
        columnMappings.put("gender", "Gender");
        columnMappings.put("race", "Race");
        columnMappings.put("educ", "Education");
        columnMappings.put("faminc", "FamilyIncome");
        columnMappings.put("employ", "EmploymentStatus");
        columnMappings.put("ideo5", "Ideology");
        columnMappings.put("pid3", "PartyAffiliation");

        // Determine column locations that we want
        mapAttributeIndex(in.readLine());

        // Build header for our datasets from hashmap
        StringBuilder header = new StringBuilder();
        ArrayList<String> list = new ArrayList<>(columnMappings.values());
        for (int i = 0; i < list.size(); i++) {
            csvAppend(header, list.get(i), i == list.size() - 1);
        }
        outTraining.write(header.toString() + "\n");
        outDataset.write(header.toString() + "\n");
    }

    /**
     * Prune dataset.
     * @throws IOException when file cannot be opened
     */
    public void run() throws IOException {
        init();
        int count = 0;
        String nextLine;
        BufferedWriter out;
        while ((nextLine = in.readLine()) != null) {
            validEntry = true;
            String[] col = nextLine.split(DELIMITER);

            out = generalElections(col, count);
            //out = trainAndTest(col, count, 2006, 2018);
            if (out == null)
                continue;

            StringBuilder entry = new StringBuilder();
            // Only take what we want - certain demographics
            csvAppend(entry, ageGroup(col[indexMappings.get("Age")]), false);
            csvAppend(entry, validate(col[indexMappings.get("Gender")]), false);
            csvAppend(entry, validate(col[indexMappings.get("Race")]), false);
            csvAppend(entry, validate(col[indexMappings.get("Education")]), false);
            csvAppend(entry, validate(col[indexMappings.get("FamilyIncome")]), false);
            csvAppend(entry, validate(col[indexMappings.get("EmploymentStatus")]), false);
            csvAppend(entry, validate(col[indexMappings.get("Ideology")]), false);
            csvAppend(entry, party(col[indexMappings.get("PartyAffiliation")]), true);

            // If all of these attributes are not null, write to appropriate file
            if (validEntry) {
                out.write(entry.toString() + "\n");
                count++;
            }
        }

        in.close();
        outTraining.close();
        outDataset.close();
    }

    public BufferedWriter generalElections(String[] columns, int count) {
        int year = Integer.parseInt(columns[1]);
        if (year % 4 != 0)
            return null;

        if (count % 4 == 0)
            return outDataset;
        else
            return outTraining;
    }

    public BufferedWriter trainAndTest(String[] columns, int count, int trainYear, int testYear) {
        int year = Integer.parseInt(columns[1]);
        if (!(year == trainYear || year == testYear))
            return null;

        if (year == testYear)
            return outDataset;
        else
            return outTraining;
    }

    /**
     * Get index values for selected attributes.
     * @param header The header of the dataset.
     */
    public void mapAttributeIndex(String header) {
        String[] columns = header.split(DELIMITER);
        for (int i = 0; i < columns.length; i++) {
            if (columnMappings.containsKey(columns[i])) {
                indexMappings.put(columnMappings.get(columns[i]), i);
            }
        }
    }

    /**
     * Ensure a string is not empty. If empty, invalidate the entry.
     * @param val String to be checked
     * @return String that was checked
     */
    public String validate(String val) {
        if (val.isEmpty()) {
            validEntry = false;
        }
        return val;
    }

    /**
     * Categorize age into multiple groups for classification.
     * @param val Age
     * @return Age group
     */
    public String ageGroup(String val) {
        validate(val);
        int age = Integer.parseInt(val);
        if (age >= 18 && age < 30) {
            return "18-29";
        } else if (age >= 30 && age < 45) {
            return "30-44";
        } else if (age >= 45 && age < 65) {
            return "45-64";
        } else {
            return "65+";
        }
    }

    /**
     * Invalidate entry if the party is not Democrat or Republican.
     * @param val Political party
     * @return Political party
     */
    public String party(String val) {
        validate(val);
        if (!(val.equals("Democrat") || val.equals("Republican"))) {
            validEntry = false;
        }
        return val;
    }

    /**
     * Append a value to a CSV line.
     * @param sb String to append to
     * @param append Selection to append
     * @param last If this is the last column
     */
    public void csvAppend(StringBuilder sb, String append, boolean last) {
        sb.append(append);
        if (!last)
            sb.append(",");
    }
}
