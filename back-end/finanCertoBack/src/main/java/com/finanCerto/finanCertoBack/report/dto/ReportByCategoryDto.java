package com.finanCerto.finanCertoBack.report.dto;

import com.finanCerto.finanCertoBack.report.ExportFormat;

public record ReportByCategoryDto(String token, ExportFormat format) {
}
