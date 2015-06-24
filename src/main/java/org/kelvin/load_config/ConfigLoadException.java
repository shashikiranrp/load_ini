package org.kelvin.load_config;

/**
 * @author <a href="mailto:shasrp@yahoo-inc.com">Shashikiran</a>
 */
public class ConfigLoadException extends RuntimeException
{
    public ConfigLoadException() {
    }

    public ConfigLoadException(String message) {
        super(message);
    }

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigLoadException(Throwable cause) {
        super(cause);
    }

    public ConfigLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
