package kr.co.zeppy.global.error;

public class ExpiredJwtException extends ZeppyException{
    
    public ExpiredJwtException(ZeppyError error) {
        super(error);
    }
}
