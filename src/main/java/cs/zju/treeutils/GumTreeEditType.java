package cs.zju.treeutils;

public enum GumTreeEditType {
    INSERT,
    DELETE,
    UPDATE,
    MOVE_TREE,
    SUBSTITUTE, // We add a new action: nodes are substituted by other different nodes
    SHOULD_IGNORE,
    MULTI_EDITS,
    MOVE_AND_UPDATE
}
