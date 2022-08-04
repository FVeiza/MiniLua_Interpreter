
package interpreter.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import interpreter.value.Value;
import interpreter.value.NumberValue;
import interpreter.value.TableValue;
import interpreter.expr.TableEntry;


public class TableExpr extends Expr{
    
    private List<TableEntry> table = new ArrayList<>();
    
    public TableExpr(int line){
        super(line);
    }
    
    public void addEntry(TableEntry entry){
        table.add(entry);
    }
    
    @Override
    public Value<?> expr(){
        Map<Value<?>, Value<?>> map = new HashMap<Value<?>, Value<?>>();
        
        int contador = 1;
        
        for(TableEntry te : table){
            Expr key = te.key;
            Expr value = te.value;
            
            if(key == null){
                map.put(new NumberValue(Double.valueOf(contador)), value.expr());
                contador++;
            }
            else{
                map.put(key.expr(), value.expr());
            }
        }
        
        TableValue tv = new TableValue(map);
        
        return tv;
    }
}
