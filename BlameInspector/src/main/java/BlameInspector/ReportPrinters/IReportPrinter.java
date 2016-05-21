package blameinspector.reportprinters;

import blameinspector.TicketInfo;

import java.util.ArrayList;
import java.util.List;

public interface IReportPrinter {
    void printTickets(final List<TicketInfo> results);

    void flush();
}
