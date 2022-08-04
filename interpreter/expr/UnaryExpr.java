package interpreter.expr;

import java.util.Map;
import java.util.Scanner;

import interpreter.util.Utils;
import interpreter.value.NumberValue;
import interpreter.value.StringValue;
import interpreter.value.Value;
import interpreter.value.BooleanValue;
import interpreter.value.TableValue;

public class UnaryExpr extends Expr {
    
    private Expr expr;
    private UnaryOp op;
    private static Scanner in;
    
    static {
        in = new Scanner(System.in);
    }

    public UnaryExpr(int line, Expr expr, UnaryOp op) {
        super(line);
        this.expr = expr;
        this.op = op;
    }

    @Override
    public Value<?> expr() {
        Value<?> v = expr != null ? expr.expr() : null;

        Value<?> ret = null;
        switch (op) {
            case Neg:
                ret = negOp(v);
                break;
            case Size:
                ret = sizeOp(v);
                break;
            case Not:
                ret = notOp(v);
                break;
            case Read:
                ret = readOp(v);
                break;
            case ToNumber:
                ret = toNumberOp(v);
                break;
            case ToString:
                ret = toStringOp(v);
                break;
            default:
                Utils.abort(super.getLine());
        }

        return ret;
    }

    public Value<?> negOp(Value<?> v) {
        Value<?> ret = null;
        if (v instanceof NumberValue) {
            NumberValue nv = (NumberValue) v;
            Double d = -nv.value();
            
            ret = new NumberValue(d);
        } else if (v instanceof StringValue) {
            StringValue sv = (StringValue) v;
            String s = sv.value();

            try {
                Double d = -Double.valueOf(s);
                ret = new NumberValue(d);
            } catch (Exception e) {
                Utils.abort(super.getLine());
            }
        } else {
            Utils.abort(super.getLine());
        }

        return ret;
    }
    
    public Value<?> notOp(Value<?> v) {
        boolean b = (v == null || !v.eval());
        BooleanValue bv = new BooleanValue(b);
    
        return bv;
    }
    
    public Value<?> sizeOp(Value<?> v){
        Value<?> ret = null;
        
        if(v instanceof TableValue) {
            TableValue tv = (TableValue) v;
            Map m = tv.value();
            Integer i = m.size();
            
            ret = new NumberValue(i.doubleValue());
        } else {
            Utils.abort(super.getLine());
        }
        
        return ret;
    }
    
    public Value<?> readOp(Value<?> v){
        Value<?> ret = null;
        
        if (v instanceof StringValue) {
            StringValue sv = (StringValue) v;
            System.out.print(sv.value());

        } else {
            Utils.abort(super.getLine());
        }
        
        String str = in.nextLine().trim();
        ret = new StringValue(str);
  
        return ret;
    }
            
    public Value<?> toNumberOp(Value<?> v){
        Value<?> ret = null;
        
        if (v instanceof StringValue) {
            StringValue sv = (StringValue) v;
            String s = sv.value();

            try {
                Double d = Double.valueOf(s);
                ret = new NumberValue(d);
            } catch (Exception e) {
                Utils.abort(super.getLine());
            }
        } 
        else if(v instanceof NumberValue){
            NumberValue nv = (NumberValue) v;
            ret = new NumberValue(nv.value());
        }
        else {
            Utils.abort(super.getLine());
        }
        
        return ret;
    }
           
    public Value<?> toStringOp(Value<?> v){
        Value<?> ret = null;
        
        if (v instanceof NumberValue) {
            NumberValue nv = (NumberValue) v;
            Double d = nv.value();
            
            try {
                String s = Double.toString(d);
                ret = new StringValue(s);
            } catch (Exception e) {
                Utils.abort(super.getLine());
            }
        } 
        else if(v instanceof StringValue){
            StringValue sv = (StringValue) v;
            ret = new StringValue(sv.value());
        }
        else {
            Utils.abort(super.getLine());
        }
        
        return ret;
    }

}
