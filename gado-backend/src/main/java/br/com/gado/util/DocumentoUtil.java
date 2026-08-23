package br.com.gado.util;

/** Validação de CPF/CNPJ pelo algoritmo oficial de dígito verificador (mod 11). Sem consulta externa. */
public final class DocumentoUtil {

    private DocumentoUtil() {
    }

    public static boolean isCpfValido(String cpf) {
        String digitos = somenteDigitos(cpf);
        if (digitos.length() != 11 || todosDigitosIguais(digitos)) {
            return false;
        }

        int primeiroDigito = calcularDigitoVerificador(digitos.substring(0, 9), new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2});
        int segundoDigito = calcularDigitoVerificador(digitos.substring(0, 9) + primeiroDigito,
                new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2});

        return digitos.equals(digitos.substring(0, 9) + primeiroDigito + segundoDigito);
    }

    public static boolean isCnpjValido(String cnpj) {
        String digitos = somenteDigitos(cnpj);
        if (digitos.length() != 14 || todosDigitosIguais(digitos)) {
            return false;
        }

        int[] pesosPrimeiroDigito = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundoDigito = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int primeiroDigito = calcularDigitoVerificador(digitos.substring(0, 12), pesosPrimeiroDigito);
        int segundoDigito = calcularDigitoVerificador(digitos.substring(0, 12) + primeiroDigito, pesosSegundoDigito);

        return digitos.equals(digitos.substring(0, 12) + primeiroDigito + segundoDigito);
    }

    /** Aceita CPF (11 dígitos) ou CNPJ (14 dígitos) — usa o comprimento para decidir qual validar. */
    public static boolean isCpfOuCnpjValido(String valor) {
        String digitos = somenteDigitos(valor);
        if (digitos.length() == 11) {
            return isCpfValido(digitos);
        }
        if (digitos.length() == 14) {
            return isCnpjValido(digitos);
        }
        return false;
    }

    private static int calcularDigitoVerificador(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosDigitosIguais(String digitos) {
        return digitos.chars().distinct().count() == 1;
    }

    private static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
