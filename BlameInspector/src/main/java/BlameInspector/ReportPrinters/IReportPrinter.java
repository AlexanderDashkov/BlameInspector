package blameinspector.reportprinters;

import blameinspector.TicketInfo;

import java.util.ArrayList;

public interface IReportPrinter {
    void printTickets(final ArrayList<TicketInfo> results);

    void flush();
}
