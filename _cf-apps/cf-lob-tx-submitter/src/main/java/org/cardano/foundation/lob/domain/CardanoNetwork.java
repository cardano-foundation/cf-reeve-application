package org.cardano.foundation.lob.domain;

import java.util.Arrays;
import java.util.List;

public enum CardanoNetwork {

    MAIN, // main-net
    PREPROD, // preprod-net
    PREVIEW, // preview-net
    DEV; // e.g. locally hosted cardano-node

    public static List<String> supportedNetworks() {
        return Arrays.stream(CardanoNetwork.values()).map(network -> network.name().toLowerCase()).toList();
    }

}
