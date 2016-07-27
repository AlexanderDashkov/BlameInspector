package blameinspector.reportprinters;


public interface IHtmlStructureStorage {

    public final String HTML_HEAD = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<style>\n" +
            "body {\n" +
            "  font-family: \"Helvetica Neue\", Helvetica, Arial;\n" +
            "  font-size: 14px;\n" +
            "  line-height: 20px;\n" +
            "  font-weight: 400;\n" +
            "  color: #3b3b3b;\n" +
            "  -webkit-font-smoothing: antialiased;\n" +
            "  font-smoothing: antialiased;\n" +
            "  background: #2b2b2b;\n" +
            "}"+
            ".wrapper {\n" +
            "  margin: 0 auto;\n" +
            "  padding: 40px;\n" +
            "  max-width: 800px;\n" +
            "}\n" +
            "\n" +
            ".table {\n" +
            "  margin: 0 0 40px 0;\n" +
            "  width: 100%;\n" +
            "  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);\n" +
            "  display: table;\n" +
            "}\n" +
            "@media screen and (max-width: 580px) {\n" +
            "  .table {\n" +
            "    display: block;\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            ".row {\n" +
            "  display: table-row;\n" +
            "  background: #f6f6f6;\n" +
            "}\n" +
            ".row:nth-of-type(odd) {\n" +
            "  background: #e9e9e9;\n" +
            "}\n" +
            ".row.header {\n" +
            "  font-weight: 900;\n" +
            "  color: #ffffff;\n" +
            "  background: #ea6153;\n" +
            "}\n" +
            ".row.green {\n" +
            "  background: #27ae60;\n" +
            "}\n" +
            ".row.blue {\n" +
            "  background: #2980b9;\n" +
            "}\n" +
            "@media screen and (max-width: 580px) {\n" +
            "  .row {\n" +
            "    padding: 8px 0;\n" +
            "    display: block;\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            ".cell {\n" +
            "  padding: 6px 12px;\n" +
            "  display: table-cell;\n" +
            "}\n" +
            "@media screen and (max-width: 580px) {\n" +
            "  .cell {\n" +
            "    padding: 2px 12px;\n" +
            "    display: block;\n" +
            "  }\n" +
            "}\n"+
            "h3 {" +
            "color:#C7C7C7;" +
            "}\n" +
            "h4{" +
            "color: #D4D4D4;"+
            "}" +
            "\n" +
            ".button {\n" +
            "  display: inline-block;\n" +
            "  height: 10px;\n" +
            "  line-height: 10px;\n" +
            "  padding-top: 0px;\n" +
            "  padding-right: 10px;\n" +
            "  padding-left: 30px;\n" +
            "  position: relative;\n" +
            "  background-color:rgb(41,127,184);\n" +
            "  color:rgb(255,255,255);\n" +
            "  text-decoration: none;\n" +
            "  letter-spacing: 1px;\n" +
            "  margin-bottom: 2px;\n" +
            "  font-size: 10px;\n" +
            "  \n" +
            "  \n" +
            "  border-radius: 5px;\n" +
            "  -moz-border-radius: 5px;\n" +
            "  -webkit-border-radius: 5px;\n" +
            "  text-shadow:0px 1px 0px rgba(0,0,0,0.5);\n" +
            "-ms-filter:\"progid:DXImageTransform.Microsoft.dropshadow(OffX=0,OffY=1,Color=#ff123852,Positive=true)\";zoom:1;\n" +
            "filter:progid:DXImageTransform.Microsoft.dropshadow(OffX=0,OffY=1,Color=#ff123852,Positive=true);\n" +
            "\n" +
            "  -moz-box-shadow:0px 2px 2px rgba(0,0,0,0.2);\n" +
            "  -webkit-box-shadow:0px 2px 2px rgba(0,0,0,0.2);\n" +
            "  box-shadow:0px 2px 2px rgba(0,0,0,0.2);\n" +
            "  -ms-filter:\"progid:DXImageTransform.Microsoft.dropshadow(OffX=0,OffY=2,Color=#33000000,Positive=true)\";\n" +
            "filter:progid:DXImageTransform.Microsoft.dropshadow(OffX=0,OffY=2,Color=#33000000,Positive=true);\n" +
            "}\n" +
            "\n" +
            ".button span {\n" +
            "  position: absolute;\n" +
            "  left: 0;\n" +
            "  width: 15px;\n" +
            "  height: 10px;\n" +
            "  font-size: 10px;\n" +
            "  background-color:rgba(0,0,0,0.5);\n" +
            "  padding-top: 0px;\n" +
            "  padding-left: 5px;\n" +
            "  \n" +
            "  -webkit-border-top-left-radius: 5px;\n" +
            "-webkit-border-bottom-left-radius: 5px;\n" +
            "-moz-border-radius-topleft: 5px;\n" +
            "-moz-border-radius-bottomleft: 5px;\n" +
            "border-top-left-radius: 5px;\n" +
            "border-bottom-left-radius: 5px;\n" +
            "border-right: 1px solid  rgba(0,0,0,0.15);\n" +
            "}\n" +
            "\n" +
            ".button:hover span, .button.active span {\n" +
            "  background-color:rgb(0,102,26);\n" +
            "  border-right: 1px solid  rgba(0,0,0,0.3);\n" +
            "}\n" +
            "\n" +
            ".button:active {\n" +
            "  margin-top: 2px;\n" +
            "  margin-bottom: 5px;\n" +
            "\n" +
            "  -moz-box-shadow:0px 1px 0px rgba(255,255,255,0.5);\n" +
            "-webkit-box-shadow:0px 1px 0px rgba(255,255,255,0.5);\n" +
            "box-shadow:0px 1px 0px rgba(255,255,255,0.5);\n" +
            "-ms-filter:\"progid:DXImageTransform.Microsoft.dropshadow(OffX=0,OffY=1,Color=#ccffffff,Positive=true)\";\n" +
            "filter:progid:DXImageTransform.Microsoft.dropshadow(OffX=0,OffY=1,Color=#ccffffff,Positive=true);\n" +
            "}" +
            "</style>\n" +
            "<title>blameinspector Report page</title>" +
            "</head>\n" +
            "<body>\n";
    public final String  HTML_START = "<h3>{0} project report.</h3>\n" +
            "<h4>Date of last analyze: <br>{1}</br></h4>"+
            "\n" +
            "<div class=\"table\">\n" +
            "  <div class=\"row green header\">\n" +
            "    <div class=\"cell\">Ticket number</div>\n" +
            "    <div class=\"cell\">StackTrace </div>    \n" +
            "    <div class=\"cell\">Assignees by stackTrace </div>    \n" +
            "    <div class=\"cell\">Errors, if occured </div>\n" +
            "    <div class=\"cell\">Duplicates unresolved </div>\n" +
            "    <div class=\"cell\">Duplicates resolved </div>\n" +
            "    <div class=\"cell\">Duplicates assignees </div>\n" +
            "   </div>\n";
    public final String HTML_END =
            "</div>\n" +
                    "<h4> Summary. Tickets: All : {0}, Assigned : {1} </h4>" +
                    "</body>\n" +
                    "</html>";

    public final String TABLE_ELEM = "  <div class=\"row\">\n" +
            "    <div class=\"cell\"><a href=\"{0}\" id = \"{1}\"># {2}</a></div>\n" +
            "    <div class=\"cell\">{3}</div> \n" +
            "    <div class=\"cell\">{4}</div>\n" +
            "    <div class=\"cell\">{5}</div>\n" +
            "    <div class=\"cell\">{6}</div>\n" +
            "    <div class=\"cell\">{7}</div>\n" +
            "    <div class=\"cell\">{8}</div>\n" +
            "</div>\n";
    ;
    public final String HREF_ELEM = "<a href=\"{0}\">{1}</a>";
    public final String BUTTON_ELEM = "<a href=\"{0}\" class=\"button\"><span>✖</span>{1}</a>";
}
