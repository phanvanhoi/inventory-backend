package manage.store.inventory.ai.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import manage.store.inventory.ai.model.AiIntent;
import manage.store.inventory.ai.model.ExtractedParams;
import manage.store.inventory.ai.model.QueryResult;
import manage.store.inventory.ai.repository.AiInventoryRepository;
import manage.store.inventory.ai.repository.AiInventoryRepository.AiBalanceProjection;
import manage.store.inventory.ai.repository.AiInventoryRepository.AiTransactionProjection;
import manage.store.inventory.ai.repository.AiInventoryRepository.AiUnitComparisonProjection;

/**
 * Route intent + params tới predefined repository queries.
 * Chỉ gọi các truy vấn đã định nghĩa sẵn, KHÔNG sinh SQL động.
 */
@Component
public class QueryRouter {

    private final AiInventoryRepository aiInventoryRepository;

    public QueryRouter(AiInventoryRepository aiInventoryRepository) {
        this.aiInventoryRepository = aiInventoryRepository;
    }

    public QueryResult route(AiIntent intent, ExtractedParams params) {
        long start = System.currentTimeMillis();
        QueryResult result = new QueryResult();
        result.setIntent(intent);

        switch (intent) {
            case QUERY_BALANCE -> handleQueryBalance(params, result);
            case QUERY_NEGATIVE -> handleQueryNegative(params, result);
            case EXPLAIN_BALANCE -> handleExplainBalance(params, result);
            case COMPARE_UNITS -> handleCompareUnits(params, result);
            case UNKNOWN -> handleUnknown(result);
        }

        result.setExecutionTimeMs(System.currentTimeMillis() - start);
        return result;
    }

    private void handleQueryBalance(ExtractedParams params, QueryResult result) {
        if (params.getProductId() == null || params.getUnitId() == null) {
            result.setQueryUsed("findBalanceByUnit");
            result.setSource("inventory_balance_by_unit");
            result.setData(List.of());
            result.setRowCount(0);
            return;
        }

        List<AiBalanceProjection> balances = aiInventoryRepository.findBalanceByUnit(
                params.getProductId(),
                params.getUnitId(),
                params.getStyleName(),
                params.getSizeValue(),
                params.getLengthCode()
        );

        result.setQueryUsed("findBalanceByUnit");
        result.setSource("inventory_balance_by_unit");
        result.setData(toBalanceData(balances));
        result.setRowCount(balances.size());
    }

    private void handleQueryNegative(ExtractedParams params, QueryResult result) {
        if (params.getProductId() == null) {
            result.setQueryUsed("findNegativeBalance");
            result.setSource("inventory_negative_balance");
            result.setData(List.of());
            result.setRowCount(0);
            return;
        }

        List<AiBalanceProjection> negatives;
        if (params.getUnitId() != null) {
            negatives = aiInventoryRepository.findNegativeBalanceByUnit(
                    params.getProductId(), params.getUnitId());
            result.setQueryUsed("findNegativeBalanceByUnit");
            result.setSource("inventory_negative_balance_by_unit");
        } else {
            negatives = aiInventoryRepository.findNegativeBalanceGlobal(params.getProductId());
            result.setQueryUsed("findNegativeBalanceGlobal");
            result.setSource("inventory_negative_balance_global");
        }

        result.setData(toBalanceData(negatives));
        result.setRowCount(negatives.size());
    }

    private void handleExplainBalance(ExtractedParams params, QueryResult result) {
        if (params.getProductId() == null || params.getUnitId() == null) {
            result.setQueryUsed("findTransactionsByUnitAndVariant");
            result.setSource("inventory_transactions");
            result.setData(List.of());
            result.setRowCount(0);
            return;
        }

        // First get current balance for the variant
        List<AiBalanceProjection> currentBalance = aiInventoryRepository.findBalanceByUnit(
                params.getProductId(),
                params.getUnitId(),
                params.getStyleName(),
                params.getSizeValue(),
                params.getLengthCode()
        );

        // If we have a specific variant, get its transactions
        Long variantId = params.getVariantId();
        if (variantId == null && !currentBalance.isEmpty()) {
            variantId = currentBalance.get(0).getVariantId();
        }

        List<Map<String, Object>> data = new ArrayList<>();

        // Add current balance summary
        if (!currentBalance.isEmpty()) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("type", "balance_summary");

            int totalIn = 0;
            int totalOut = 0;
            for (AiBalanceProjection b : currentBalance) {
                int qty = b.getActualQuantity() != null ? b.getActualQuantity() : 0;
                summary.put("variant", b.getStyleName() + " size " + b.getSizeValue() + " " + b.getLengthCode());
                summary.put("balance", qty);
            }
            data.add(summary);
        }

        // Add transaction history
        if (variantId != null) {
            List<AiTransactionProjection> transactions = aiInventoryRepository
                    .findTransactionsByUnitAndVariant(params.getProductId(), params.getUnitId(), variantId);

            int totalIn = 0;
            int totalOut = 0;
            for (AiTransactionProjection tx : transactions) {
                Map<String, Object> txMap = new LinkedHashMap<>();
                txMap.put("type", "transaction");
                txMap.put("requestType", tx.getRequestType());
                txMap.put("quantity", tx.getQuantity());
                txMap.put("createdAt", tx.getCreatedAt());
                txMap.put("note", tx.getNote());
                txMap.put("setName", tx.getSetName());
                data.add(txMap);

                if ("IN".equals(tx.getRequestType())) {
                    totalIn += tx.getQuantity();
                } else if ("OUT".equals(tx.getRequestType())) {
                    totalOut += tx.getQuantity();
                }
            }

            // Add calculation summary
            Map<String, Object> calcSummary = new LinkedHashMap<>();
            calcSummary.put("type", "calculation");
            calcSummary.put("totalIn", totalIn);
            calcSummary.put("totalOut", totalOut);
            calcSummary.put("balance", totalIn - totalOut);
            data.add(calcSummary);
        }

        result.setQueryUsed("findTransactionsByUnitAndVariant");
        result.setSource("inventory_transactions");
        result.setData(data);
        result.setRowCount(data.size());
    }

    private void handleCompareUnits(ExtractedParams params, QueryResult result) {
        if (params.getProductId() == null) {
            result.setQueryUsed("findBalanceSummaryByAllUnits");
            result.setSource("inventory_unit_comparison");
            result.setData(List.of());
            result.setRowCount(0);
            return;
        }

        List<AiUnitComparisonProjection> comparisons = aiInventoryRepository
                .findBalanceSummaryByAllUnits(params.getProductId());

        List<Map<String, Object>> data = new ArrayList<>();
        for (AiUnitComparisonProjection c : comparisons) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("unitId", c.getUnitId());
            row.put("unitName", c.getUnitName());
            row.put("totalBalance", c.getTotalBalance());
            row.put("variantCount", c.getVariantCount());
            data.add(row);
        }

        result.setQueryUsed("findBalanceSummaryByAllUnits");
        result.setSource("inventory_unit_comparison");
        result.setData(data);
        result.setRowCount(comparisons.size());
    }

    private void handleUnknown(QueryResult result) {
        result.setQueryUsed(null);
        result.setSource(null);
        result.setData(List.of());
        result.setRowCount(0);
    }

    private List<Map<String, Object>> toBalanceData(List<AiBalanceProjection> balances) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (AiBalanceProjection b : balances) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("variantId", b.getVariantId());
            row.put("styleName", b.getStyleName());
            row.put("sizeValue", b.getSizeValue());
            row.put("lengthCode", b.getLengthCode());
            row.put("actualQuantity", b.getActualQuantity());
            data.add(row);
        }
        return data;
    }
}
