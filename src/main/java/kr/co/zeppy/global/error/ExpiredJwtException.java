package kr.co.zeppy.global.error;

public class ExpiredJwtException extends ApplicationException {
    
    public ExpiredJwtException(ApplicationError error) {
        super(error);
    }
}
