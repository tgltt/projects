package com.uc.phantom.bra.framework.common;

/**
 * Error Code Constants.
 * @Author Tam.
 * Modifications :
 * 2017/4/18 : Created.
 */
public class ErrorCode {

    public final static int PARSER_RESULT_OK               = 0;
    public final static int PARSER_RESULT_PARTIAL_OK      = -1;
    public final static int PARSER_RESULT_FAILED           = -2;
    public final static int PARSER_RESULT_BUGREPORT_NOT_EXISTS                 = -3;
    public final static int PARSER_RESULT_BUGREPORT_NONE_CONTENT_NEED_PARSED = -4;
    public final static int PARSER_RESULT_PARAMS_ILLEGAL     = -5;
    public final static int PARSER_RESULT_NO_PROPER_HANDLER  = -6;

    public final static int PARSER_RESULT_NOT_SUPPORT_METHOD = -7;
    public final static int PARSER_RESULT_IGNORE       = -8;
    public final static int PARSER_NOT_SUPPORTED_TYPE = -9;

}
