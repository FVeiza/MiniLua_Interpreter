
package interpreter.command;

import interpreter.expr.Expr;
import interpreter.value.Value;
import interpreter.expr.Variable;
import interpreter.expr.SetExpr;
import interpreter.expr.AccessExpr;
import interpreter.util.Utils;
import interpreter.value.TableValue;

public class GenericForCommand extends Command{
    
    private Variable var1;
    private Variable var2;
    private Expr expr;
    private Command cmds;
    
    public GenericForCommand(int line, Variable var1, Expr expr, Command cmds){
        this(line, var1, null, expr, cmds);
        
    }
    
    public GenericForCommand(int line, Variable var1, Variable var2, Expr expr, Command cmds){
        super(line);
        this.var1 = var1;
        this.var2 = var2;
        this.expr = expr;
        this.cmds = cmds;
    }
    
    @Override
    public void execute(){
        TableValue tv = null;

        if(expr.expr() instanceof TableValue){
            tv = (TableValue) expr.expr();
        } else {
            Utils.abort(super.getLine());
        }
        
        for(Value<?> aux : tv.value().keySet()){
            var1.setValue(aux);
            var2.setValue(tv.value().get(aux));
            cmds.execute();
        }   
    }
}
