package br.com.gado.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Confere uma senha em texto puro contra o hash armazenado — BCrypt (esquema atual de
 * SUsuario#login), com compatibilidade para hashes SHA-256 e senhas antigas em texto puro que
 * ainda não passaram pelo login (e por isso não foram migradas para BCrypt). Extraído aqui para
 * ser reaproveitado pela dupla validação de segurança do módulo financeiro
 * (SDocumentoEntrada.editarNfe) sem duplicar a lógica de hash.
 */
public final class SenhaUtil {

    private static final Pattern SHA256_HEX = Pattern.compile("^[a-fA-F0-9]{64}$");
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

    private SenhaUtil() {
    }

    public static boolean confere(String senhaInformada, String senhaArmazenada) {
        if (senhaInformada == null || senhaInformada.isBlank()
                || senhaArmazenada == null || senhaArmazenada.isBlank()) {
            return false;
        }

        if (isBCrypt(senhaArmazenada)) {
            return BCRYPT_ENCODER.matches(senhaInformada, senhaArmazenada);
        }
        if (isSha256Hex(senhaArmazenada)) {
            return senhaArmazenada.equalsIgnoreCase(sha256Hex(senhaInformada));
        }
        return senhaArmazenada.equals(senhaInformada);
    }

    private static boolean isBCrypt(String value) {
        return BCRYPT.matcher(value).matches();
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
