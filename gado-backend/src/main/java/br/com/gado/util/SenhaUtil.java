package br.com.gado.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Confere uma senha em texto puro contra o hash armazenado, usando o mesmo esquema
 * (SHA-256 hex, com compatibilidade para senhas antigas em texto puro) de SUsuario#login.
 * Extraído aqui para ser reaproveitado pela dupla validação de segurança do módulo
 * financeiro (SDocumentoEntrada.editarNfe) sem duplicar a lógica de hash.
 */
public final class SenhaUtil {

    private static final Pattern SHA256_HEX = Pattern.compile("^[a-fA-F0-9]{64}$");

    private SenhaUtil() {
    }

    public static boolean confere(String senhaInformada, String senhaArmazenada) {
        if (senhaInformada == null || senhaInformada.isBlank()
                || senhaArmazenada == null || senhaArmazenada.isBlank()) {
            return false;
        }

        if (isSha256Hex(senhaArmazenada)) {
            return senhaArmazenada.equalsIgnoreCase(sha256Hex(senhaInformada));
        }
        return senhaArmazenada.equals(senhaInformada);
    }

    private static boolean isSha256Hex(String value) {
        return SHA256_HEX.matcher(value).matches();
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar senha.", e);
        }
    }
}
