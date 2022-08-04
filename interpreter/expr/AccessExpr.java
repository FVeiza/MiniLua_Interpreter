
package interpreter.expr;

import interpreter.expr.Expr;
import interpreter.value.Value;
import interpreter.value.TableValue;
import interpreter.util.Utils;
import interpreter.value.NumberValue;

public class AccessExpr extends SetExpr{
    
    private Expr base;
    private Expr index;
    
    public AccessExpr(int line, Expr base, Expr index){
        super(line);
        this.base = base;
        this.index = index;
    }
    
    @Override
    public Value<?> expr(){
        Value<?> v = null;

        if(base.expr() instanceof TableValue){
            TableValue tv = (TableValue) base.expr(); 
            v = tv.value().get(index.expr());

        } else {
            Utils.abort(super.getLine());
        }
        
        return v;
    }
    
    @Override
    public void setValue(Value<?> value){
        if(base.expr() instanceof TableValue){
            TableValue tv = (TableValue) base.expr();
            tv.value().put(index.expr(), value);
        } else {
            Utils.abort(super.getLine());
        }
    }
}
