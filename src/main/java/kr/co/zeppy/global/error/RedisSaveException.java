package kr.co.zeppy.global.error;

public class RedisSaveException extends ApplicationException {
    public RedisSaveException(ApplicationError error) {
        super(error);
    }
}
