package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import io.hypersistence.utils.hibernate.query.SQLExtractor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;

import java.time.YearMonth;
import java.util.List;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;

@RequiredArgsConstructor
@Slf4j
public class CustomTransactionBatchRepositoryImpl implements CustomTransactionBatchRepository {
    private final EntityManager em;

    @Override
    public List<TransactionBatchEntity> findByFilter(BatchSearchRequest body) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TransactionBatchEntity> criteriaQuery = builder.createQuery(TransactionBatchEntity.class);
        Root<TransactionBatchEntity> rootEntry = criteriaQuery.from(TransactionBatchEntity.class);
        Predicate pStatus = builder.isTrue(builder.literal(true));
        Predicate pTransactionType = builder.isTrue(builder.literal(true));
        Predicate pAccountingPeriodFrom = builder.isTrue(builder.literal(true));
        Predicate pAccountingPeriodTo = builder.isTrue(builder.literal(true));
        Predicate pTransactionStatus = builder.isTrue(builder.literal(true));
        Predicate pOrganisationId = builder.equal(rootEntry.get("filteringParameters").get("organisationId"), body.getOrganisationId());

        if (!body.getStatus().isEmpty()) {
            pStatus = builder.in(rootEntry.get("status")).value(body.getStatus());
        }

        if (!body.getTransactionTypes().isEmpty()) {
            Expression<?> bitwiseAnd = builder.function("BITAND", Integer.class, rootEntry.get("filteringParameters").get("transactionTypes"), builder.literal(body.getTransactionTypes().stream().toList()));
            pTransactionType = builder.notEqual(bitwiseAnd, 0);
        }

        if (null != body.getAccountingPeriodFrom()) {
            pAccountingPeriodFrom = builder.greaterThanOrEqualTo(rootEntry.get("filteringParameters").get("accountingPeriodFrom"), body.getAccountingPeriodFrom());
        }

        if (null != body.getAccountingPeriodFrom()) {
            pAccountingPeriodTo = builder.lessThanOrEqualTo(rootEntry.get("filteringParameters").get("accountingPeriodTo"), body.getAccountingPeriodTo());
        }

        if (!body.getTxStatus().isEmpty()) {
            Join<Object, Object> transactions = rootEntry.join("transactions", JoinType.INNER);
            pTransactionStatus = builder.in(transactions.get("status")).value(body.getTxStatus());
        }

        criteriaQuery.select(rootEntry);
        criteriaQuery.where(pOrganisationId, pStatus, pTransactionType, pAccountingPeriodFrom, pAccountingPeriodTo, pTransactionStatus);


        TypedQuery<TransactionBatchEntity> theQuery = em.createQuery(criteriaQuery);

        theQuery.setMaxResults(body.getLimit());

        if (null != body.getPage() && 0 < body.getPage()) {
            body.setPage(body.getPage() - 1);
            theQuery.setFirstResult(body.getPage() * body.getLimit());
        }

        return theQuery.getResultList();

    }

}
