package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VatConvertorTest {

    private VatConvertor vatConvertor;

    @BeforeEach
    public void setUp() {
        vatConvertor = new VatConvertor();
    }

    @Test
    public void shouldWorkForSingle() {
        val vatCode = "CH-VMN-8.1";

        val result = vatConvertor.apply(vatCode);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo("CH-VMN-8.1");
    }

    @Test
    public void shouldWorkForBroken() {
        val vatCode = "CH:aaaa:bbbb";

        val result = vatConvertor.apply(vatCode);

        assertThat(result.isLeft()).isTrue();

        val problem = result.getLeft();
        assertThat(problem.getTitle()).isEqualTo("INVALID_VAT");
        assertThat(problem.getDetail()).isEqualTo("Invalid vat code");
        assertThat(problem.getParameters()).containsEntry("vat", vatCode);
    }

    @Test
    public void shouldWorkForComplex() {
        val complexVatCode = "CH-VMN-7.7 : CH-VID";

        val result = vatConvertor.apply(complexVatCode);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo("CH-VID");
    }

}
