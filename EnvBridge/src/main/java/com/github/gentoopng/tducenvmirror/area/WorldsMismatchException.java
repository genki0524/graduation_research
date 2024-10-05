package com.github.gentoopng.tducenvmirror.area;

public class WorldsMismatchException extends Exception {
    WorldsMismatchException() { }
    WorldsMismatchException(String msg) { super(msg); }
    WorldsMismatchException(Throwable cause) { super(cause); }
    WorldsMismatchException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
