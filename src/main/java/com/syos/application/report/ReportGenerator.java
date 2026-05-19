package com.syos.application.report;

public abstract class ReportGenerator implements IReportGenerator {
    
    @Override
    public final String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append(getReportHeader());
        report.append("\n");
        report.append(generateReportBody());
        report.append("\n");
        report.append(getReportFooter());
        return report.toString();
    }

    protected abstract String getReportHeader();
    protected abstract String generateReportBody();
    
    protected String getReportFooter() {
        return "=".repeat(80) + "\n" + "End of Report\n" + "=".repeat(80);
    }
}
