package pl.easyoffer.offer_service.exception;

public class DuplicateOfferException extends RuntimeException {
    public DuplicateOfferException(String message) {
        super(message);
    }
}
