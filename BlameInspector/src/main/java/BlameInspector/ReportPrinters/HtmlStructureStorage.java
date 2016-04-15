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
            "    background-color: #21CC46;\n" +
            "    color: white;\n" +
            "}\n" +
            "h3 {" +
            "color:#4050FF;"+
            "}\n" +
            "</style>\n" +
            "<title>BlameInspector Report file</title>"+
            "</head>\n" +
            "<body>\n" +
            "<h3>BlameInspector report.</h3>"+
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

    public final String TABLE_ELEM = "  <tr>\n" +
            "    <td><a href=\"{0}\">Ticket # {1}</a></th>\n" +
            "    <td><a href=\"{2}\">{3}</a></th>    \n" +
            "    <td>{4}</th>\n" +
            "  </tr>\n";;
}
