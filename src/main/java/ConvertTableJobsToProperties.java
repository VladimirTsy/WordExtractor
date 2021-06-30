import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;

public class ConvertTableJobsToProperties {

    public static final Logger LOGGER = Logger.getLogger(Domain.class);

    public static void main(String[] args) throws IOException, InvalidFormatException {

        StringBuilder sb = new StringBuilder();

        String pathToSourceFile = "C:\\Users\\мвидео\\Documents\\_БИ Телеком\\DB Entities\\AR and Collection\\Letters\\";
        String fileName = "PRODN-104604-Letter Entities - V1.0.docx";
        String fileNameResult = fileName.substring(0, fileName.length() -4) + "vmpref.properties";

        FileInputStream fis = new FileInputStream(pathToSourceFile  + fileName);

        ArrayList<String> allJobName = new ArrayList<>();

        //VIP - OP Run Book - Vimpelcom.docx

        //VIP - CSM Run Book.docx

        // VIP - AR Runbook.docx;

        // Switch Control Runbook.docx
        // MPS Running Comments.docx
        // VIP - Billing Run Book.docx

        // HWPFDocument doc = new HWPFDocument(fis);
        XWPFDocument docx = new XWPFDocument(OPCPackage.open(fis));


        Iterator<IBodyElement> iter = docx.getBodyElementsIterator();

        /** цикл по всем элементам документа  */
        int countTableRecord = 0;
        while (iter.hasNext()) {

            IBodyElement elem = iter.next();
            if (elem instanceof XWPFTable) {

                XWPFTable table = (XWPFTable) elem;
                String jobName = "";

               if( table.getRows().size() > 0) {

                   if( table.getRow(0)
                           .getCell(0)
                           .getText()
                           .toLowerCase(Locale.ROOT)
                           .replaceAll("\\s","").contains("jobname") &&

                           ! ( table.getRow(0)
                                   .getCell(1)
                                   .getText()
                                   .toLowerCase(Locale.ROOT)
                                   .replaceAll("\\s","").contains("dependency") ||
                              table.getRow(0)
                                           .getCell(1)
                                           .getText()
                                           .toLowerCase(Locale.ROOT)
                                           .replaceAll("\\s","").contains("group")))
                   {

                       // записываем информацию из таблицы
                       for (int j = 0; j < table.getRows().size(); j++) {
                           XWPFTableRow r = table.getRow(j);

                           if (j == 0) {
                               jobName = r.getCell(1).getText().replaceAll("\\s", "");

                               jobName = Processing.GetValidNameJob(jobName);

                               sb.append(jobName + "." + r.getCell(0).getText().replaceAll("\\s", "_") + "=" + jobName);
                               sb.append("\n");
                           } else if (r.getCell(0).getText().trim().length() > 0){



                               sb.append(jobName + "." + r.getCell(0).getText().replaceAll("\\s+$", "").replaceAll("\\s", "_") + "=" + Processing.getTextFromCell(r.getCell(1)));
                               sb.append("\n");
                           }

                       }
                       allJobName.add(jobName);

                       sb.append("\n");
                       countTableRecord++;
                   }else{
                       LOGGER.log(Level.WARN, "Обнаружена таблица " + table.getRow(0)
                               .getCell(0)
                               .getText() + " не описывающая Job");
                   }
               }else{
                   LOGGER.log(Level.WARN, "Обнаружена таблица не имеющая строк");

               }

            }
        }
        // записываем вимена всех джобов в конце файла
        sb.append("JOBS=" + allJobName.stream().map(n -> String.valueOf(n))
                                  .collect(Collectors.joining(",")));
        sb.append("\n");

        Write.writeToFile(sb, pathToSourceFile + fileNameResult);
        LOGGER.log(Level.INFO, "Обработано и записано: " + countTableRecord + " таблиц");

    }
}