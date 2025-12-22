package com.finanCerto.finanCertoBack.relatorio;

import com.finanCerto.finanCertoBack.categoria.Tipo;
import com.finanCerto.finanCertoBack.relatorio.dto.RelatorioPorCategoria;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
    private final RestTemplate restTemplate = new RestTemplate();
    private  final TokenService tokenService;
    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    public RelatorioController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/relatorioPorCategoria")
    public ResponseEntity<byte[]> gerarRelatorioCustomizado(
            @RequestParam String formato,
            @RequestParam Tipo tipo,
            @RequestHeader("Authorization") String token) {

        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        String url = String.format(
                "%s/relatorioPorCategoria?usuario_id=%s&tipo=%s&formato=%s",
                fastApiBaseUrl,usuario.getId(), tipo, formato);



        // Faz a requisição GET e recebe o arquivo como byte[]
        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                byte[].class
        );


        MediaType contentType = response.getHeaders().getContentType();


        return ResponseEntity.ok()
                .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=relatorio." + formato)
                .body(response.getBody());
    }
    @GetMapping("/relatorioSaldoMensal")
    public ResponseEntity<byte[]> gerarRelatorioSaldoMensal(@RequestParam String formato,
                                                            @RequestHeader("Authorization") String token){
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        String url = String.format(
                "%srelatorioSaldoMensal?usuario_id=%s&formato=%s",
                fastApiBaseUrl,usuario.getId(), formato);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                byte[].class
        );


        MediaType contentType = response.getHeaders().getContentType();


        return ResponseEntity.ok()
                .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=relatorio." + formato)
                .body(response.getBody());
    }
    @GetMapping("/relatorioTransacaoDetalhado")
    public ResponseEntity<byte[]> gerarRelatoriosTransacaoDetalhado(@RequestParam String formato,
                                                                    @RequestHeader("Authorization") String token){
        Usuario usuario = tokenService.obterUsuario(token.replace("Bearer ", ""));
        String url = String.format(
                "%s/relatorioTransacaoDetalhado?usuario_id=%s&formato=%s",
                fastApiBaseUrl,usuario.getId(), formato);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                byte[].class
        );


        MediaType contentType = response.getHeaders().getContentType();


        return ResponseEntity.ok()
                .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=relatorio." + formato)
                .body(response.getBody());
    }

}
