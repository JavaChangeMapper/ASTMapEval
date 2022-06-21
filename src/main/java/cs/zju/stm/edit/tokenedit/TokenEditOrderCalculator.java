package cs.zju.stm.edit.tokenedit;

import com.github.gumtreediff.matchers.MappingStore;
import cs.zju.stm.TokenRange;
import cs.zju.stm.edit.tokenedit.actions.TokenEditAction;
import cs.zju.utils.Pair;

import java.util.*;

public class TokenEditOrderCalculator {
    private List<TokenRange> srcRanges;
    private List<TokenRange> dstRanges;
    private Set<TokenRange> srcRangeSet;
    private Set<TokenRange> dstRangeSet;

    private List<TokenEditAction> actions;
    private List<TokenEditAction> orderedActions;
    private MappingStore stmtMs;
    private Map<TokenRange, List<TokenEditAction>> srcRangeActionMap;
    private Map<TokenRange, List<TokenEditAction>> dstRangeActionMap;
    private List<Pair<TokenRange, TokenRange>> matchedRangePairs;

    public TokenEditOrderCalculator(List<TokenRange> srcRanges, List<TokenRange> dstRanges,
                                    MappingStore stmtMs, List<TokenEditAction> actions,
                                    List<Pair<TokenRange, TokenRange>> matchedRangePairs){
        this.srcRanges = srcRanges;
        this.dstRanges = dstRanges;
        if (srcRanges != null)
            srcRangeSet = new HashSet<>(srcRanges);
        if (dstRanges != null)
            dstRangeSet = new HashSet<>(dstRanges);
        this.actions = actions;
        this.stmtMs = stmtMs;
        this.matchedRangePairs = matchedRangePairs;
        srcRangeActionMap = new HashMap<>();
        dstRangeActionMap = new HashMap<>();
        initActionMap();
        reorderActions();
    }

    private void initActionMap(){
        for (TokenEditAction action: actions){
            List<TokenRange> srcRangesOfAction = action.getSrcRanges();
            List<TokenRange> dstRangesOfAction = action.getDstRanges();
            List<TokenRange> exchangeRangesOfAction = action.getExchangeRanges();
            List<TokenRange> exchangeDstRangesOfAction = action.getExchangeDstRanges();

            if (srcRangesOfAction != null && srcRangeSet != null){
                for (TokenRange range: srcRangesOfAction){
                    if (srcRangeSet.contains(range)){
                        if (!srcRangeActionMap.containsKey(range))
                            srcRangeActionMap.put(range, new ArrayList<>());
                        srcRangeActionMap.get(range).add(action);
                    }
                }
            }

            if (exchangeRangesOfAction != null && srcRangeSet != null) {
                for (TokenRange range: exchangeRangesOfAction) {
                    if (srcRangeSet.contains(range)){
                        if (!srcRangeActionMap.containsKey(range))
                            srcRangeActionMap.put(range, new ArrayList<>());
                        srcRangeActionMap.get(range).add(action);
                    }
                }
            }

            if (dstRangesOfAction != null && dstRangeSet != null){
                for (TokenRange range: dstRangesOfAction){
                    if (dstRangeSet.contains(range)){
                        if (!dstRangeActionMap.containsKey(range))
                            dstRangeActionMap.put(range, new ArrayList<>());
                        dstRangeActionMap.get(range).add(action);
                    }
                }
            }

            if (exchangeDstRangesOfAction != null && dstRangeSet != null) {
                for (TokenRange range: exchangeDstRangesOfAction){
                    if (dstRangeSet.contains(range)) {
                        if (!dstRangeActionMap.containsKey(range))
                            dstRangeActionMap.put(range, new ArrayList<>());
                        dstRangeActionMap.get(range).add(action);
                    }
                }
            }
        }

        for (TokenRange range: srcRangeActionMap.keySet()){
            srcRangeActionMap.get(range).sort((o1, o2) -> {
                if (o1.isExchange() && !o2.isExchange())
                    return -1;
                if (o2.isExchange() && !o1.isExchange())
                    return 1;
                if (o1.isMove() && !o2.isMove())
                    return -1;
                if (o2.isMove() && !o1.isMove())
                    return 1;
                return 0;
            });
        }

        for (TokenRange range: dstRangeActionMap.keySet()){
            dstRangeActionMap.get(range).sort((o1, o2) -> {
                if (o1.isExchange() && !o2.isExchange())
                    return -1;
                if (o2.isExchange() && !o1.isExchange())
                    return 1;
                if (o1.isMove() && !o2.isMove())
                    return -1;
                if (o2.isMove() && !o1.isMove())
                    return 1;
                return 0;
            });
        }
    }

    private void reorderActions(){
        orderedActions = new ArrayList<>();
        Map<TokenRange, TokenRange> matchRangeMap = new HashMap<>();
        for (Pair<TokenRange, TokenRange> pair: matchedRangePairs){
            matchRangeMap.put(pair.first, pair.second);
        }
        TokenRange curRange = null;
        if (dstRanges != null && dstRanges.size() > 0)
            curRange = dstRanges.get(0);
        if (srcRanges != null) {
            for (TokenRange srcRange : srcRanges) {
                TokenRange dstRange = matchRangeMap.get(srcRange);
                List<TokenEditAction> actions = srcRangeActionMap.get(srcRange);
                if (actions != null) {
                    for (TokenEditAction action : actions) {
                        if (orderedActions.contains(action))
                            continue;
                        orderedActions.add(action);
                    }
                }
                if (dstRange != null) {
                    addActionsForDstRangesBeforeRange(dstRange);
                    curRange = dstRange;
                }
            }
        }
        if (curRange != null) {
            int curIdx = dstRanges.indexOf(curRange);
            for (;curIdx < dstRanges.size(); curIdx++) {
                TokenRange range = dstRanges.get(curIdx);
                List<TokenEditAction> actions = dstRangeActionMap.get(range);
                if (actions == null)
                    continue;

                for (TokenEditAction action: actions){
                    if (orderedActions.contains(action))
                        continue;
                    orderedActions.add(action);
                }
            }
        }
    }

    private void addActionsForDstRangesBeforeRange(TokenRange dstRange){
        for (TokenRange range: dstRanges){
            if (range.equals(dstRange))
                break;
            List<TokenEditAction> actions = dstRangeActionMap.get(range);
            if (actions != null) {
                for (TokenEditAction action : actions) {
                    if (orderedActions.contains(action))
                        continue;
                    orderedActions.add(action);
                }
            }
        }
    }

    public List<TokenEditAction> getOrderedActions() {
        return orderedActions;
    }
}
