package org.cardanofoundation.lob.app.blockchain_publisher.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class WithExtraIds<T> {

    private Set<String> ids;

    private T companion;

}
