package blameinspector.reportprinters;


public interface IHtmlStructureStorage {

    public final String HTML_HEAD = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<style>\n" +
            "table {\n" +
            "    width:100%;\n" +
            "}\n" +
            "table, th, td {\n" +
            "    border: 1px solid black;\n" +
            "    border-collapse: collapse;\n" +
            "}\n" +
            "th, td {\n" +
            "    padding: 5px;\n" +
            "    text-align: left;\n" +
            "}\n" +
            "table#t01 tr:nth-child(even) {\n" +
            "    background-color: #eee;\n" +
            "}\n" +
            "table#t01 tr:nth-child(odd) {\n" +
            "   background-color:#fff;\n" +
            "}\n" +
            "table#t01 th  {\n" +
            "    background-color: #21CC46;\n" +
            "    color: white;\n" +
            "}\n" +
            "h3 {" +
            "color:#4050FF;" +
            "}\n" +
            "</style>\n" +
            "<title>blameinspector Report file</title>" +
            "</head>\n" +
            "<body>\n";
    public final String  HTML_START = "<h3>{0} project report.</h3>" +
            "\n" +
            "<table id=\"t01\">\n" +
            "  <tr>\n" +
            "    <th>Ticket Number</th>\n" +
            "    <th>StackTrace </th>    \n" +
            "    <th>Assignees by stackTrace </th>    \n" +
            "    <th>Errors, if occured </th>\n" +
            "    <th>Duplicates unresolved </th>\n" +
            "    <th>Duplicates resolved </th>\n" +
            "    <th>Duplicates assignees </th>\n" +
            "  </tr>\n";
    public final String HTML_END =
            "</table>\n" +
                    "\n" +
                    "<br> Summary. Tickets: All : {0}, Assigned : {1} </br>" +
                    "</body>\n" +
                    "</html>";

    public final String TABLE_ELEM = "  <tr>\n" +
            "    <td><a href=\"{0}\" id = \"{1}\">Ticket # {2}</a></th>\n" +
            "    <td>{3}</th> \n" +
            "    <td>{4}</th>\n" +
            "    <td>{5}</th>\n" +
            "    <td>{6}</th>\n" +
            "    <td>{7}</th>\n" +
            "    <td>{8}</th>\n" +
            "  </tr>\n";
    ;
    public final String HREF_ELEM = "<a href=\"{0}\">{1}</a>";
}
