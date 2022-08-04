
package interpreter.expr;

public class TableEntry {
    
    public Expr key;
    public Expr value;
    
    public TableEntry(Expr key, Expr value){
        this.key = key;
        this.value = value;
    }
}
