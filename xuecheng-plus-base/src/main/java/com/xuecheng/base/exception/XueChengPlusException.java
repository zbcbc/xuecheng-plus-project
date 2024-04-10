package com.xuecheng.base.exception;

/**
 * ClassName: XueChengPlusException
 * Package: com.xuecheng.base.execption
 * Description:
 *
 * @Author zbc
 * @Create 2024/2/26 14:21
 * @Version 1.0
 */
public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException() {
        super();
    }
    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }
}
