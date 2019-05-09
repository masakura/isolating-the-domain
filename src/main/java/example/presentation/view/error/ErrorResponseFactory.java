package example.presentation.view.error;

public interface ErrorResponseFactory {

    ErrorResponse create(Exception exception);
}
