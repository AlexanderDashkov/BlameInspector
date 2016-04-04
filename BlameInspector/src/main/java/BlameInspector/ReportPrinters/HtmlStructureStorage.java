package BlameInspector.ReportPrinters;


public interface HtmlStructureStorage {

    public final String HTML_START = "<!DOCTYPE html>\n" +
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
            "    background-color: black;\n" +
            "    color: white;\n" +
            "}\n" +
            "</style>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" + "<br>\n" +
            "\n" +
            "<table id=\"t01\">\n" +
            "  <tr>\n" +
            "    <th>Ticket Number</th>\n" +
            "    <th>Assignee </th>    \n" +
            "    <th>Error, if occured </th>\n" +
            "  </tr>\n";
    public final String HTML_END =
            "</table>\n" +
            "\n" +
             "<br> Summary. Tickets: All : {0}, Assigned : {1} </br>"+
            "</body>\n" +
            "</html>";

    public final String TABLE_ELEM ="  <tr>\n" +
            "    <th>Ticket # {0}</th>\n" +
            "    <th>{1}</th>    \n" +
            "    <th>{2}</th>\n" +
            "  </tr>\n";;
}
