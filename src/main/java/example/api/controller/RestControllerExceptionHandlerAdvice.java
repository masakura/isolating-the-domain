package example.api.controller;

import example.api.view.error.ErrorResponse;
import example.api.view.error.ErrorType;
import example.infrastructure.datasource.employee.EmployeeNotFoundException;
import example.presentation.controller.BaseControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * 例外の型に応じたエラーレスポンスを作成するAdvice
 */
@RestControllerAdvice(annotations = RestController.class)
public class RestControllerExceptionHandlerAdvice {

    static final Logger logger = LoggerFactory.getLogger(BaseControllerAdvice.class);

    ResourceBundle resource;

    public RestControllerExceptionHandlerAdvice() throws Exception {
        resource = ResourceBundle.getBundle("errorResponse", Locale.ROOT);
    }

    // 複数の例外に同じ処理をする場合
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<ErrorResponse> handleValidationError(Exception exception) {
        logger.info("入力エラー", exception);
        return errorResponse(exception);
    }

    // メッセージを動的にする場合
    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String fieldNames = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .collect(Collectors.joining(", "));
        return errorResponse(exception, fieldNames);
    }

    // 独自例外の場合
    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleEmployeeNotFoundException(EmployeeNotFoundException exception) {
        return errorResponse(exception);
    }

    // 例外の親クラスで処理する場合
    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException exception) {
        logger.error("システムエラー", exception);
        // このケースでは Object#getClass() は使用できない
        return errorResponse(DataAccessException.class);
    }

    // どこにもハンドリングされなかった場合
    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleOtherException(Exception exception) {
        logger.error("不明なエラー", exception);
        // 可能な限り例外が発生しない方法でエラーレスポンスを構築する
        return new ErrorResponse(ErrorType.システムエラー, "E99999", "不明なエラーが発生しました。").toEntity();
    }

    ResponseEntity<ErrorResponse> errorResponse(Throwable exception, Object... args) {
        return errorResponse(exception.getClass(), args);
    }

    ResponseEntity<ErrorResponse> errorResponse(Class<?> clz, Object... args) {
        return errorResponse(clz.getName(), args);
    }

    ResponseEntity<ErrorResponse> errorResponse(String exceptionClassName, Object... args) {
        String message = resource.getString(exceptionClassName + ".message");
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorType.valueOf(resource.getString(exceptionClassName + ".type")),
                resource.getString(exceptionClassName + ".code"),
                MessageFormat.format(message, args)
        );
        return errorResponse.toEntity();
    }
}
