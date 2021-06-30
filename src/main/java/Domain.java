import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Domain {

    public static final Logger LOGGER = Logger.getLogger(Domain.class);

    public static void main(String[] args) throws IOException, InvalidFormatException {

       StringBuilder sb = new StringBuilder();

        FileInputStream fis = new FileInputStream("C:\\Users\\мвидео\\Documents\\_БИ Телеком\\WordExtractor\\Customer Area Entities.docx"); // Customer Area Entities.docx");   // CSM - Reference Tables.docx"); // AR Area Entities.docx");
        // HWPFDocument doc = new HWPFDocument(fis);
        XWPFDocument docx = new XWPFDocument(OPCPackage.open(fis));


        Iterator<IBodyElement> iter = docx.getBodyElementsIterator();

        // ищем параграф - оглавление (Contents)
        while (iter.hasNext()) {
            IBodyElement elem = iter.next();
            if (elem instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) elem;
                if (paragraph.getText().toLowerCase(Locale.ROOT).contains("content")) {
                    break;
                }
            }
        }

        List<String> entities = new ArrayList<>();
        // идем по оглавлению и собираем список заголовков (сущностей)
        while (iter.hasNext()) {
            IBodyElement elem = iter.next();
            if (elem instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) elem;

                // проверяем, что заголовок яв-ся заголовком Верхнего уровня
                if (Pattern.compile("^(\\s{0,10}\\d+\\.)(?!\\s{0,3}\\d)")
                        .matcher(paragraph.getText()).find() &&
                        !paragraph.getText().toLowerCase(Locale.ROOT).contains("introduction")) {

                    // добавляем заголовок верхнего уровня в коллекцию
                    entities.add(paragraph.getText().toLowerCase(Locale.ROOT)
                            .trim().replaceAll("\\s", ""));
                }


                if (paragraph.getText().toLowerCase(Locale.ROOT)
                        .trim().startsWith("introduction")) {
                    break;
                }
            }
        }


        String candidateTitle = "";
        String currentTitle = "";
        boolean hasAttributeDescriptions = false;
        String description = "";
        boolean descriptionEnd = false;


          /** цикл по всем элементам документа  */
          while (iter.hasNext()) {

            IBodyElement elem = iter.next();


            if (elem instanceof XWPFParagraph) {

                XWPFParagraph paragraph = (XWPFParagraph) elem;
                String key = paragraph.getText().toLowerCase(Locale.ROOT).trim().replaceAll("\\s", "");


                // Обработка исключительной ситуациии, Когда для заголовка не найдена таблица
               if(key.length() > 0){

                   ArrayList<String> list = (ArrayList<String>) entities.stream()
                           .filter(x -> x.contains(key))
                           .collect(Collectors.toList());
                   // Если флаги не были сброшены от предыдущего заголовка,
                   // значит для предыдущего заголовка не была найдена
                   // соответсвующая таблица, а текущий (следующий) заголовок НАЙДЕН!
                   // Это исключительный случай, логируем ошибку
                   if(list.size() > 0 && currentTitle.length() > 0 || hasAttributeDescriptions){

                           LOGGER.log(Level.ERROR, "Для сущности " + currentTitle + " не была найдена соотвествующая таблица. " +
                                   "Таблицу необходимо добавить вручную.");

                       hasAttributeDescriptions = false;
                       currentTitle = "";

                       description = "";
                       descriptionEnd = false;

                   }
               }

                // устанавливаем текущее имя заголовка-таблицы (сущности)
               if (key.length() > 0 && currentTitle.length() == 0 && !hasAttributeDescriptions) {

                    ArrayList<String> list = (ArrayList<String>) entities.stream()
                            .filter(x -> x.contains(key))
                            .collect(Collectors.toList());

                    if (list.size() > 0) {
                        currentTitle = paragraph.getText();

                        if(currentTitle.contains("SUBSCRIBER_HISTORY")){
                            System.out.println("stop");
                        }
                    }

                    // Обработка исключительной ситуации,
                    // когда заголовок забыли включить в Contents
                   // при такой длине будем предполагать, что это пропущенный заголовок
                    if(key.length() > 2 && key.length() < 51 && candidateTitle.length() == 0){
                        candidateTitle = key;
                    }else if(candidateTitle.length() > 0){

                        // если условие верно, значит действительно был попущенный заголовок
                        // и о нем написано в следующем параграфе
                        if( key.contains(candidateTitle) && ( key.contains("entity") || key.contains("table"))){
                            currentTitle = candidateTitle;
                            candidateTitle = "";

                            description = description + (description .length() > 0 ? "<br/>" + paragraph.getText() : paragraph.getText());

                            descriptionEnd = false;

                        }
                    }

               }  // ищем параграф, в котором содержится информация с нужной таблицей
                 else if (key.contains("attributedescriptions")) {
                   hasAttributeDescriptions = true;
               }


                   /**  собираем description о таблице
                    *  */
                String validText = paragraph.getText().replaceAll("\\s","").toLowerCase(Locale.ROOT);

                  if(currentTitle.length() > 0 &&
                           (!validText.contains("entitylifecycle") &&
                           !validText.contains("relationships") &&
                           !validText.contains("attributedescriptions") &&
                           !validText.contains(currentTitle.replaceAll("\\s","").toLowerCase(Locale.ROOT))
                           )  && !descriptionEnd
                  ){
                     description = description + (description .length() > 0 ? "<br/>" + paragraph.getText() : paragraph.getText());

                     // устанавливаем флаг, о том что description собран
                   }else if(validText.contains("entitylifecycle") || validText.contains("relationships") ||
                             validText.contains("attributedescriptions")) {
                      descriptionEnd = true;
                   }


                // все условия выполнены, ожидаем необходимую таблицу
            } else if (elem instanceof XWPFTable
                    && currentTitle.length() > 0
                    && hasAttributeDescriptions) {

                // приводим имя заголовка к валидной форме
                currentTitle = Processing.toValidForm(currentTitle);

                XWPFTable table = (XWPFTable) elem;

                // убеждаемся в том, что таблица содержит
                // хотя бы одну строку
                if(table.getRows().size() > 0 ) {

                    // определяем индексы нужных колонок
                    XWPFTableRow row = table.getRow(0);
                    int idxAttribute = -1;
                    int idxDescription = -1;
                    int idxLifeCycle = -1;
                    int idxValuesEdits = -1;
                    int idxValidValues = -1;
                    boolean findfild = false;
                    for (int i = 0; i < row.getTableCells().size(); i++) {

                        String c = row.getCell(i).getText().toLowerCase(Locale.ROOT)
                                   .trim().replaceAll("\\s", "").replaceAll("\\&", "");

                        if ( (c.contains("field") || c.contains("attribute") || c.contains("columnname")) && !findfild ) {
                            idxAttribute = i;
                            findfild = true;
                        } else if (c.contains("description")) {
                            idxDescription = i;
                        } else if(c.contains("lifecycle")){
                            idxLifeCycle = i;
                        }else if(c.contains("valuesedits")){
                            idxValuesEdits = i;
                        }else if(c.contains("validvalues")){
                            idxValidValues = i;
                        }


                    }

                    // логируем ошибку, если не нашлись критически важные поля
                    if (idxAttribute < 0 ) {
                        LOGGER.log(Level.ERROR, "Нет нужных колонок в таблице " + currentTitle);

                        // скидываем флаги в исходное состояние
                        currentTitle = "";
                        hasAttributeDescriptions = false;
                        candidateTitle = "";
                        description = "";
                        descriptionEnd = false;

                    } else {

                        // записываем информацию о description
                        sb.append(currentTitle.replace("\r", "").replaceAll("[\\n\\t ]", "").toUpperCase(Locale.ROOT) + "=" + description);
                            sb.append("\n");

                            // записываем информацию из таблицы
                        for (int j = 1; j < table.getRows().size(); j++) {
                            XWPFTableRow r = table.getRow(j);


                            sb.append(currentTitle.replace("\r", "").replaceAll("[\\n\\t ]", "").toUpperCase(Locale.ROOT) + "."
                                    + r.getCell(idxAttribute).getText().replace("\n", "").replaceAll("\r", "").replaceAll("[\\n\\t ]", "").trim().replaceAll("\u00A0", "")
                                    + "=" + (idxDescription >=0 ? Processing.getTextFromCell(r.getCell(idxDescription)).replaceAll("[\\r\\n]+", "<br/>") : ""));

                            sb.append("\n");

                            if(idxLifeCycle > 0) {
                                sb.append(currentTitle.replace("\r", "").replaceAll("[\\n\\t ]", "").toUpperCase(Locale.ROOT) + "."
                                        + r.getCell(idxAttribute).getText().replaceAll("\n", "").replaceAll("\r", "").replaceAll("[\\n\\t ]", "") + "."
                                        + "LifeCycle=" + r.getCell(idxLifeCycle).getText());
                                sb.append("\n");
                            }

                            if(idxValuesEdits > 0) {
                                sb.append(currentTitle.replace("\r", "").replaceAll("[\\n\\t ]", "").toUpperCase(Locale.ROOT) + "."
                                        + r.getCell(idxAttribute).getText().replaceAll("\n", "").replaceAll("\r", "").replaceAll("[\\n\\t ]", "") + "."
                                        + "ValuesEdits=" + r.getCell(idxValuesEdits).getText().replaceAll("[\\r\\n]+", "<br/>"));
                                sb.append("\n");
                            }


                            if(idxValidValues > 0) {



                                   sb.append(currentTitle.replace("\r", "").replaceAll("[\\n\\t ]", "").toUpperCase(Locale.ROOT) + "."
                                           + r.getCell(idxAttribute).getText().replaceAll("\n", "").replaceAll("\r", "").replaceAll("[\\n\\t ]", "").replaceAll("\\r\\n|\\r|\\n", " ") + "."
                                           + "ValidValues=" + r.getCell(idxValidValues).getText().replaceAll("\r", "<br/>").replaceAll("[\\r\\n]+", "<br/>"));
                                   sb.append("\n");

                            }

                            //INVOICE_ITEM.BAN.LifeCycle=Set when the invoice item is created
                        }

                        // таблица записана, сбрасываем флаги в исходное состояние
                        currentTitle = "";
                        hasAttributeDescriptions = false;
                        candidateTitle = "";
                        description = "";
                        descriptionEnd = false;

                        sb.append("\n");


                    }
                }
            }
        }

       Write.writeToFile(sb);
    }


}

