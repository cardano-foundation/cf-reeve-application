package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;

class AccountEventCodesConversionTaskItemTest {

    private PipelineTaskItem taskItem;

    private OrganisationPublicApiIF organisationPublicApiIF;

    @BeforeEach
    public void setup() {
        this.organisationPublicApiIF = mock(OrganisationPublicApiIF.class);
        this.taskItem = new AccountEventCodesConversionTaskItem(
                (passedOrganisationTransactions, ignoredOrganisationTransactions, allViolationUntilNow) -> null,
                organisationPublicApiIF
        );
    }

//    @Test
//    public void test() {
//        val txId = Transaction.id("1", "1");
//
//        val txs = TransactionWithViolations.create(Transaction.builder()
//                .id(txId)
//                .internalTransactionNumber("1")
//                .organisation(Organisation.builder().id("1").build())
//                .transactionType(FxRevaluation)
//                .items(
//                        Set.of(TransactionItem.builder()
//                                .id(TransactionItem.id(txId, "0"))
//                                .accountCodeDebit(Optional.of("1"))
//                                .build()
//                        ))
//                .build());
//
//        val newTx = taskItem.run(txs);
//
//
//        assertThat(newTx.violations()).isEmpty();
//    }

}
