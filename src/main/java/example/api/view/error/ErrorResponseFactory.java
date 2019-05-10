package example.api.view.error;

public interface ErrorResponseFactory {

    ErrorResponse create(Exception exception);
}
