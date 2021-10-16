/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io;

import me.tecc.lahc.io.connectors.DefaultConnector;
import me.tecc.lahc.io.connectors.NonBlockingConnector;

import java.util.ArrayList;
import java.util.List;

public class Connectors {
    private static final List<Connector> connectors = new ArrayList<>();
    private static Connector defaultConnector;

    public static Connector createDefault() {
        Connector connector = new DefaultConnector();
        connectors.add(connector);
        return connector;
    }

    public static void stopAll() {
        for (Connector connector : connectors) {
            try {
                connector.destroy();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static Connector getDefault() {
        if (defaultConnector == null) {
            defaultConnector = createDefault();
        }
        return defaultConnector;
    }
}
