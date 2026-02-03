package com.finanCerto.finanCertoBack.relatorio;

import com.finanCerto.finanCertoBack.categoria.Tipo;
import com.finanCerto.finanCertoBack.relatorio.dto.RelatorioPorCategoria;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios financeiros")
public class RelatorioController {
    
    private static final Logger logger = LoggerFactory.getLogger(RelatorioController.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final TokenService tokenService;
    
    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;
    
    
    private static final String REPORT_ENDPOINT_PREFIX = "/";
    private static final String USUARIO_ID_PARAM = "usuario_id";
    private static final String FORMATO_PARAM = "formato";
    private static final String TIPO_PARAM = "tipo";

    public RelatorioController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

  
    private void validarFormato(String formato) {
        if (formato == null || formato.isEmpty()) {
            throw new IllegalArgumentException("Formato não pode estar vazio");
        }
        String formatoNormalizado = formato.toLowerCase();
        if (!formatoNormalizado.matches("^(excel|pdf|xlsx|xls)$")) {
            throw new IllegalArgumentException(
                String.format("Formato inválido: '%s'. Use 'excel' ou 'pdf'", formato)
            );
        }
    }

    private String construirUrl(String endpoint, Long usuarioId, String... parametrosAdicionais) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl)
            .path(REPORT_ENDPOINT_PREFIX + endpoint)
            .queryParam(USUARIO_ID_PARAM, usuarioId);
        
      
        for (int i = 0; i < parametrosAdicionais.length; i += 2) {
            if (i + 1 < parametrosAdicionais.length) {
                uriBuilder.queryParam(parametrosAdicionais[i], parametrosAdicionais[i + 1]);
            }
        }
        
        return uriBuilder.toUriString();
    }

    
    private ResponseEntity<byte[]> executarRequisicaoRelatorio(String url, String nomeRelatorio, String formato) {
        try {
            logger.info("Requisitando relatório ao FastAPI: {}", url);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                byte[].class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("Erro ao gerar relatório - Status: {}", response.getStatusCode());
                throw new RuntimeException(
                    String.format("Erro ao gerar relatório: %s", response.getStatusCode())
                );
            }
            
        
            MediaType contentType = response.getHeaders().getContentType();
            if (contentType == null) {
                contentType = inferirMediaType(formato);
            }
            
            logger.info("Relatório gerado com sucesso. Tipo: {}, Tamanho: {} bytes", 
                contentType, 
                response.getBody() != null ? response.getBody().length : 0
            );
            
           
            String nomeArquivo = String.format("%s.%s", nomeRelatorio, formato.toLowerCase());
            
            return ResponseEntity.ok()
                .contentType(contentType)
                .header("Content-Disposition", String.format("attachment; filename=\"%s\"", nomeArquivo))
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .body(response.getBody());
            
        } catch (RestClientException e) {
            logger.error("Erro de comunicação com FastAPI ao requisitar relatório", e);
            throw new RuntimeException(
                String.format("Erro ao comunicar com serviço de análise: %s", e.getMessage()), e
            );
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar relatório", e);
            throw new RuntimeException(
                String.format("Erro inesperado ao gerar relatório: %s", e.getMessage()), e
            );
        }
    }

 
    private MediaType inferirMediaType(String formato) {
        String formatoNormalizado = formato.toLowerCase();
        switch (formatoNormalizado) {
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

   
    @GetMapping("/relatorioPorCategoria")
    @Operation(
        summary = "Gerar relatório de transações por categoria",
        description = "Gera um relatório de transações agrupadas por categoria no formato solicitado (Excel ou PDF)"
    )
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    @ApiResponse(responseCode = "500", description = "Erro ao gerar relatório")
    public ResponseEntity<byte[]> gerarRelatorioCustomizado(
            @RequestParam(name = "formato", defaultValue = "excel") 
            @Parameter(description = "Formato do relatório: 'excel' ou 'pdf'")
            String formato,
            
            @RequestParam(name = "tipo", required = false)
            @Parameter(description = "Tipo de transação: RECEITA, DESPESA ou null para todas")
            Tipo tipo,
            
            @RequestHeader("Authorization")
            @Parameter(description = "Token JWT no formato 'Bearer <token>'")
            String token) {
        
        try {
           
            validarFormato(formato);
         
            Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
            logger.info("Gerando relatório por categoria para usuário: {}", usuario.getId());
      
            String url = construirUrl(
                "relatorioPorCategoria",
                usuario.getId(),
                TIPO_PARAM, String.valueOf(tipo),
                FORMATO_PARAM, formato
            );
            
            return executarRequisicaoRelatorio(url, "relatorio_categoria", formato);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validação falhou: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório por categoria", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

  
    @GetMapping("/relatorioSaldoMensal")
    @Operation(
        summary = "Gerar relatório de saldo mensal",
        description = "Gera um relatório com saldo mensal (receitas, despesas e saldo)"
    )
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    @ApiResponse(responseCode = "500", description = "Erro ao gerar relatório")
    public ResponseEntity<byte[]> gerarRelatorioSaldoMensal(
            @RequestParam(name = "formato", defaultValue = "excel")
            @Parameter(description = "Formato do relatório: 'excel' ou 'pdf'")
            String formato,
            
            @RequestParam(name = "ano", required = false)
            @Parameter(description = "Ano para filtrar o relatório (opcional)")
            String ano,
            
            @RequestHeader("Authorization")
            @Parameter(description = "Token JWT no formato 'Bearer <token>'")
            String token) {
        
        try {
            
            validarFormato(formato);
            
          
            Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
            logger.info("Gerando relatório de saldo mensal para usuário: {}", usuario.getId());
            
            
            String url = construirUrl(
                "relatorioSaldoMensal",
                usuario.getId(),
                FORMATO_PARAM, formato,
                "ano", ano
            );
            
            return executarRequisicaoRelatorio(url, "relatorio_saldo_mensal", formato);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validação falhou: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de saldo mensal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

   
    @GetMapping("/relatorioTransacaoDetalhado")
    @Operation(
        summary = "Gerar relatório de transações detalhadas",
        description = "Gera um relatório detalhado com todas as informações das transações"
    )
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    @ApiResponse(responseCode = "500", description = "Erro ao gerar relatório")
    public ResponseEntity<byte[]> gerarRelatorioTransacaoDetalhado(
            @RequestParam(name = "formato", defaultValue = "excel")
            @Parameter(description = "Formato do relatório: 'excel' ou 'pdf'")
            String formato,
            
            @RequestParam(name = "dataInicio", required = false)
            @Parameter(description = "Data inicial do filtro (YYYY-MM-DD)")
            String dataInicio,
            
            @RequestParam(name = "dataFim", required = false)
            @Parameter(description = "Data final do filtro (YYYY-MM-DD)")
            String dataFim,
            
            @RequestHeader("Authorization")
            @Parameter(description = "Token JWT no formato 'Bearer <token>'")
            String token) {
        
        try {
          
            validarFormato(formato);
            
          
            Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
            logger.info("Gerando relatório detalhado para usuário: {}", usuario.getId());
            
           
            String url = construirUrl(
                "relatorioTransacaoDetalhado",
                usuario.getId(),
                FORMATO_PARAM, formato,
                "data_inicio", dataInicio,
                "data_fim", dataFim
            );
            
            return executarRequisicaoRelatorio(url, "relatorio_transacoes", formato);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validação falhou: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório detalhado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
