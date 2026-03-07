package com.finanCerto.finanCertoBack.report;

import com.finanCerto.finanCertoBack.category.CategoryType;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints for generating financial reports")
@RequiredArgsConstructor
public class ReportController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TokenService tokenService;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    private static final String REPORT_ENDPOINT_PREFIX = "/";
    private static final String USER_ID_PARAM = "usuario_id";
    private static final String FORMAT_PARAM = "formato";
    private static final String TYPE_PARAM = "tipo";

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof User user) {
            return user;
        }
        if (principal instanceof User user) {
            return user;
        }
        throw new RuntimeException("User not authenticated");
    }

    private void validateFormat(String format) {
        if (format == null || format.isEmpty()) {
            throw new IllegalArgumentException("Format cannot be empty");
        }
        String normalizedFormat = format.toLowerCase();
        if (!normalizedFormat.matches("^(excel|pdf|xlsx|xls)$")) {
            throw new IllegalArgumentException(
                    String.format("Invalid format: '%s'. Use 'excel' or 'pdf'", format)
            );
        }
    }

    private String buildUrl(String endpoint, Long userId, String... additionalParams) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl)
                .path(REPORT_ENDPOINT_PREFIX + endpoint)
                .queryParam(USER_ID_PARAM, userId);

        for (int i = 0; i < additionalParams.length; i += 2) {
            if (i + 1 < additionalParams.length) {
                uriBuilder.queryParam(additionalParams[i], additionalParams[i + 1]);
            }
        }

        return uriBuilder.toUriString();
    }

    private ResponseEntity<byte[]> executeReportRequest(String url, String reportName, String format) {
        try {
            log.info("Requesting report from FastAPI: {}", url);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Error generating report - Status: {}", response.getStatusCode());
                throw new RuntimeException(
                        String.format("Error generating report: %s", response.getStatusCode())
                );
            }

            MediaType contentType = response.getHeaders().getContentType();
            if (contentType == null) {
                contentType = inferMediaType(format);
            }

            log.info("Report generated successfully. Type: {}, Size: {} bytes",
                    contentType,
                    response.getBody() != null ? response.getBody().length : 0
            );

            String fileName = String.format("%s.%s", reportName, format.toLowerCase());

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName))
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .body(response.getBody());

        } catch (RestClientException e) {
            log.error("Communication error with FastAPI when requesting report", e);
            throw new RuntimeException(
                    String.format("Error communicating with analysis service: %s", e.getMessage()), e
            );
        } catch (Exception e) {
            log.error("Unexpected error generating report", e);
            throw new RuntimeException(
                    String.format("Unexpected error generating report: %s", e.getMessage()), e
            );
        }
    }

    private MediaType inferMediaType(String format) {
        String normalizedFormat = format.toLowerCase();
        switch (normalizedFormat) {
            case "excel":
            case "xlsx":
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "xls":
                return MediaType.valueOf("application/vnd.ms-excel");
            case "pdf":
                return MediaType.APPLICATION_PDF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @GetMapping("/category")
    @Operation(
            summary = "Generate transaction report by category",
            description = "Generates a report of transactions grouped by category in the requested format (Excel or PDF)"
    )
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid parameters")
    @ApiResponse(responseCode = "500", description = "Error generating report")
    public ResponseEntity<byte[]> generateCategoryReport(
            @RequestParam(name = "format", defaultValue = "excel")
            @Parameter(description = "Report format: 'excel' or 'pdf'")
            String format,

            @RequestParam(name = "type", required = false)
            @Parameter(description = "Transaction type: INCOME, EXPENSE or null for all")
            CategoryType type,

            @RequestParam(name = "categoryId", required = false)
            @Parameter(description = "Specific category ID (optional)")
            String categoryId) {

        try {
            validateFormat(format);
            User user = getAuthenticatedUser();
            log.info("Generating category report for user: {}", user.getId());

            String url;
            if (categoryId != null) {
                url = buildUrl(
                        "relatorioPorCategoria",
                        user.getId(),
                        "categoria_id", categoryId,
                        FORMAT_PARAM, format
                );
            } else if (type != null) {
                url = buildUrl(
                        "relatorioPorCategoria",
                        user.getId(),
                        "tipo", String.valueOf(type),
                        FORMAT_PARAM, format
                );
            } else {
                url = buildUrl(
                        "relatorioPorCategoria",
                        user.getId(),
                        FORMAT_PARAM, format
                );
            }

            return executeReportRequest(url, "category_report", format);

        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error generating category report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/monthly-balance")
    @Operation(
            summary = "Generate monthly balance report",
            description = "Generates a report with monthly balance (income, expenses, and balance)"
    )
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid parameters")
    @ApiResponse(responseCode = "500", description = "Error generating report")
    public ResponseEntity<byte[]> generateMonthlyBalanceReport(
            @RequestParam(name = "format", defaultValue = "excel")
            @Parameter(description = "Report format: 'excel' or 'pdf'")
            String format,

            @RequestParam(name = "year", required = false)
            @Parameter(description = "Year to filter report (optional)")
            String year) {

        try {
            validateFormat(format);
            User user = getAuthenticatedUser();
            log.info("Generating monthly balance report for user: {}", user.getId());

            String url = buildUrl(
                    "relatorioSaldoMensal",
                    user.getId(),
                    FORMAT_PARAM, format,
                    "ano", year
            );

            return executeReportRequest(url, "monthly_balance_report", format);

        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error generating monthly balance report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/transactions-detailed")
    @Operation(
            summary = "Generate detailed transaction report",
            description = "Generates a detailed report with all transaction information"
    )
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid parameters")
    @ApiResponse(responseCode = "500", description = "Error generating report")
    public ResponseEntity<byte[]> generateDetailedTransactionReport(
            @RequestParam(name = "format", defaultValue = "excel")
            @Parameter(description = "Report format: 'excel' or 'pdf'")
            String format,

            @RequestParam(name = "startDate", required = false)
            @Parameter(description = "Start date filter (YYYY-MM-DD)")
            String startDate,

            @RequestParam(name = "endDate", required = false)
            @Parameter(description = "End date filter (YYYY-MM-DD)")
            String endDate) {

        try {
            validateFormat(format);
            User user = getAuthenticatedUser();
            log.info("Generating detailed report for user: {}", user.getId());

            String url = buildUrl(
                    "relatorioTransacaoDetalhado",
                    user.getId(),
                    FORMAT_PARAM, format,
                    "data_inicio", startDate,
                    "data_fim", endDate
            );

            return executeReportRequest(url, "transactions_report", format);

        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error generating detailed report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
