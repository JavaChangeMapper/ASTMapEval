package cs.zju.treeutils;

public class GumTreeException extends RuntimeException {
    public GumTreeException(String info){
        super("gumtree ast comparison error:" + info);
    }
}
