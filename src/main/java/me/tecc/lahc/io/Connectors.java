/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io;

import me.tecc.lahc.io.connectors.Connector;
import me.tecc.lahc.io.connectors.DefaultConnector;

import java.util.ArrayList;
import java.util.List;

public class Connectors {
    private static final List<Connector> connectors = new ArrayList<>();

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
}
