import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.*;

public class Processing {


   public static String toValidForm(String currentTitle) {

       Matcher m = Pattern.compile("\\((.*?)\\)").matcher(currentTitle);

       if (m.find())
           return m.group(1);

       return currentTitle;
   }

   public static String getTextFromCell(XWPFTableCell cell){
       String result = "";
       for(XWPFParagraph paragraph : cell.getParagraphs()){


           result = result + (result.length() > 0 ? ("\\n" + paragraph.getText()).replaceAll("[\\r\\n]+", "\n") : paragraph.getText().replaceAll("[\\r\\n]+", "\n") );
       }
       return result;
   }


   public static  String GetValidNameJob(String jobname){
       Pattern regex = Pattern.compile("^[^\\]}(),]*");
       Matcher regexMatcher = regex.matcher(jobname);
       if (regexMatcher.find()) {
           return  regexMatcher.group().trim();
       }

       return jobname + " is invalid name";
   }


}
