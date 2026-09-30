package com.gap.api.Model.DTO;

/**
 * Envelope padrão de todas as respostas da API.
 *
 * @param message mensagem legível para o cliente
 * @param status  "success" ou "error"
 * @param data    payload (objeto, lista, mapa de erros de validação ou null)
 */
public record BaseResponse<T>(String message, String status, T data) {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(message, SUCCESS, data);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(message, ERROR, null);
    }

    public static <T> BaseResponse<T> error(String message, T data) {
        return new BaseResponse<>(message, ERROR, data);
    }
}
