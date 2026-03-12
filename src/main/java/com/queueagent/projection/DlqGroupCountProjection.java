package com.queueagent.projection;

public interface DlqGroupCountProjection {
    String getErrorType();
    Long getCnt();
}
