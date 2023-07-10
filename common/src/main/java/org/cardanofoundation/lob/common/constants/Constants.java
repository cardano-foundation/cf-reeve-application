package org.cardanofoundation.lob.common.constants;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
public class Constants {
    public static final String METADATA_DATE_PATTERN = "dd/MM/yyyy";
    public static final SimpleDateFormat METADATA_DATE_FORMAT = new SimpleDateFormat(METADATA_DATE_PATTERN);
    public static final BigInteger METADATA_LABEL = BigInteger.valueOf(512);
}
