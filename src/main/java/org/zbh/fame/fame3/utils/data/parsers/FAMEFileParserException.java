package org.zbh.fame.fame3.utils.data.parsers;

public class FAMEFileParserException extends Throwable {

    private String input;
    private String inputIdentifier;

    public FAMEFileParserException(String message) {
        super(message);

        this.input = null;
        this.inputIdentifier = null;
    }

    public FAMEFileParserException(String message, Throwable throwable) {
        super(message, throwable);

        this.input = null;
        this.inputIdentifier = null;
    }

    public FAMEFileParserException(String message, Throwable throwable, String input, String inputIdentifier) {
        super(message, throwable);

        this.input = input;
        this.inputIdentifier = inputIdentifier;
    }

    public FAMEFileParserException(Throwable throwable, String input, String inputIdentifier) {
        super(throwable);

        this.input = input;
        this.inputIdentifier = inputIdentifier;
    }

    public FAMEFileParserException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace, String input, String inputIdentifier) {
        super(message, throwable, enableSuppression, writableStackTrace);

        this.input = input;
        this.inputIdentifier = inputIdentifier;
    }

    public String getInput() {
        return input;
    }

    public String getInputIdentifier() {
        return inputIdentifier;
    }
}
