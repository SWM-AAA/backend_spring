package kr.co.zeppy.global.error;


public class NotFoundException extends ApplicationException {
    public NotFoundException(ApplicationError error) {
        super(error);
    }
}
