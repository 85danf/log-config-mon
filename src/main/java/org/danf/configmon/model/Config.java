package org.danf.configmon.model;

import java.util.Date;

public interface Config {

    long getId();
    ConfigType type();
    Date getLastUpdated();
    Date getLastChecked();
}
