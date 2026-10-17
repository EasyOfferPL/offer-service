package pl.easyoffer.offer_service.exception;

public class DatasetExportForbiddenException extends RuntimeException {

    public DatasetExportForbiddenException() {
        super("Dataset export is not authorized");
    }
}
