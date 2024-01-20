package com.employee.employeeManage;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;
/*
* Write a program that:
1. Take the file as an input
2. Programmatically analyze the file and print in console the name and position of employees
      a) who has worked for 7 consecutive days.
      b) who have less than 10 hours of time between shifts but greater than 1 hour
      c) Who has worked for more than 14 hours in a single shift
3. Write a clean code with code comments and assumptions (if any) you are making
*
* */

@SpringBootApplication
public class EmployeeManageApplication {

    public static void main(String[] args) {

        SpringApplication.run(EmployeeManageApplication.class, args);
        String excelFilePath = "/Users/rohit/Desktop/Rohit_WorkSpace/InternshalaProject/Assignment_Timecard.xlsx";
        try (FileInputStream fis = new FileInputStream(excelFilePath); Workbook workbook = new XSSFWorkbook(fis)) { // Use XSSFWorkbook for .xlsx files
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            int numRows = sheet.getPhysicalNumberOfRows();

            Map<String, Set<Integer>> map = new HashMap<>();
            Map<String,String> employeeData = new HashMap<>();

           List<String> sevenConsecutiveDays = employeeWorkingSevenConsecutive(numRows, sheet, employeeData, map);
            System.out.println("===================Five Consecutive days Working Employee ===============================================================================");
            for(String emp : sevenConsecutiveDays){
                System.out.println(emp);
            }

        } catch (IOException | EncryptedDocumentException ex) {
            ex.printStackTrace();
        }
    }

    private static List<String> employeeWorkingSevenConsecutive(int numRows, Sheet sheet, Map<String, String> employeeData, Map<String, Set<Integer>> map) {
        for (int rowIndex = 1; rowIndex < numRows; rowIndex++) { // Assuming the first row is the header

            Row row = sheet.getRow(rowIndex);
            String name  =  row.getCell(7).getStringCellValue();
            String position = row.getCell(0).getStringCellValue(); // Assuming name is in the first column

            employeeData.put(position,name);
            Cell cell = row.getCell(3); // Assuming position is in the second column

            Integer formattedDate = 0;
            if (cell.getCellType() == CellType.NUMERIC) {
                double numericCellValue = cell.getNumericCellValue();
                LocalDate localDate = LocalDate.ofEpochDay((long) numericCellValue - 1);
                // Format LocalDate to a desired date format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
                formattedDate = Integer.valueOf(localDate.format(formatter));

            }
            // If the ID is not in the map, add a new entry
            if (!map.containsKey(position)) {
                map.put(position, new HashSet<>());
            }
            // Add the value to the set associated with the ID
            map.get(position).add(formattedDate);

        }

        Map<String, Integer>  countConsecutiveMap =  countConsecutive(map);
        System.out.println(countConsecutiveMap);
        List<String> sevenConsecutiveDays = new ArrayList<>();

        // Iterate through the entries
        for (Map.Entry<String, Integer> entry : countConsecutiveMap.entrySet()) {
            if (entry.getValue() == 5) {
               // sevenConsecutiveDays.add(entry.getKey());
               String name =  employeeData.get(entry.getKey());
               String position = entry.getKey();
                sevenConsecutiveDays.add("Name : "+name + " Position : " + position + "ConsecutiveDays COUNT : "+entry.getValue()) ;

            }
        }

      return  sevenConsecutiveDays;
    }


    public static Map<String, Integer>  countConsecutive(Map<String, Set<Integer>> map) {

        // Count consecutive integers for each key
        Map<String, Integer> consecutiveCountMap = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : map.entrySet()) {
            String key = entry.getKey();
            Set<Integer> values = entry.getValue();
            int minValue = Collections.min(values);
            int consecutiveCount = countConsecutiveIntegers(values, minValue);
            consecutiveCountMap.put(key, consecutiveCount);
        }

        // Print the result
        return consecutiveCountMap;

    }

    public static int countConsecutiveIntegers(Set<Integer> values, int start) {
        int count = 0;
        // Check if the start value is in the set
        if (!values.contains(start)) {
            return count;
        }
        // Count consecutive integers
        while (values.contains(start)) {
            count++;
            start++;
        }
        return count;
    }


    private static boolean hasShortBreakBetweenShifts(Row row) {
        SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

        int timeColumnIndex = 2; // Assuming time data starts from the third column
        int numRows = row.getPhysicalNumberOfCells();

        for (int cellIndex = timeColumnIndex; cellIndex < numRows - 1; cellIndex += 2) {
            Cell startTimeCell = row.getCell(cellIndex);
            Cell endTimeCell = row.getCell(cellIndex + 1);

            if (startTimeCell != null && endTimeCell != null) {
                String startTimeStr = startTimeCell.getStringCellValue();
                String endTimeStr = endTimeCell.getStringCellValue();

                try {
                    Date startTime = TIME_FORMAT.parse(startTimeStr);
                    Date endTime = TIME_FORMAT.parse(endTimeStr);

                    long timeDifference = endTime.getTime() - startTime.getTime();
                    long hoursBetween = timeDifference / (60 * 60 * 1000);

                    if (hoursBetween > 1 && hoursBetween < 10) {
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }


}
