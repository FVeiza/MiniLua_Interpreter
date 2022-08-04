
package interpreter.command;

import interpreter.expr.Expr;
import interpreter.value.Value;
import interpreter.expr.Variable;
import interpreter.expr.SetExpr;
import interpreter.expr.AccessExpr;
import interpreter.value.NumberValue;

public class NumericForCommand extends Command{
    
    private Variable var;
    private Expr expr1;
    private Expr expr2;
    private Expr expr3;
    private Command cmds;
    
    public NumericForCommand(int line, Variable var, Expr expr1, Expr expr2, Command cmds){
        this(line, var, expr1, expr2, null, cmds);
    }
    
    public NumericForCommand(int line, Variable var, Expr expr1, Expr expr2, Expr expr3, Command cmds){
        super(line);
        this.var = var;
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.expr3 = expr3;
        this.cmds = cmds;
    }

    @Override
    public void execute(){
        Value<?> ex1 = expr1.expr();
        Value<?> ex2 = expr2.expr();
        
        double n3;
        
        if(expr3 == null){
            n3 = 1.0;
        }
        else{
            Value<?> ex3 = expr3.expr();
            n3 = Double.valueOf(ex3.toString());
        }
        
        var.setValue(ex1);
        
        double n1 = Double.valueOf(ex1.toString());
        double n2 = Double.valueOf(ex2.toString());
        
        for(; n1 <= n2; n1 += n3){
            cmds.execute();
	    NumberValue nv = new NumberValue(n1+n3);
            var.setValue(nv);
        }
    }
}
