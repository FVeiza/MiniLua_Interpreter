
package interpreter.command;

import interpreter.expr.Expr;
import interpreter.value.Value;

public class IfCommand extends Command{
    
    private Expr expr;
    private Command thenCmds;
    private Command elseCmds;
    
    public IfCommand(int line, Expr expr, Command thenCmds){
        super(line);
        this.expr = expr;
        this.thenCmds = thenCmds;
    }
    
    public void setElseCommands(Command elseCmds){
        this.elseCmds = elseCmds;
    }
    
    @Override
    public void execute(){
        Value<?> v;

        if((v = expr.expr()) != null && v.eval()){
            thenCmds.execute();
        }
        else {
            if(elseCmds != null){
                elseCmds.execute();
            }
        }
    }
}
