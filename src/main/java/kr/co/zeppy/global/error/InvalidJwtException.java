package kr.co.zeppy.global.error;

public class InvalidJwtException extends ZeppyException {
    
    public InvalidJwtException(ZeppyError error) {
        super(error);
    }
}
