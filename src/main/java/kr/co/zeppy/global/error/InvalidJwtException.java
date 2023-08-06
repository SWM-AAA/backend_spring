package kr.co.zeppy.global.error;

public class InvalidJwtException extends ApplicationException {
    
    public InvalidJwtException(ApplicationError error) {
        super(error);
    }
}
