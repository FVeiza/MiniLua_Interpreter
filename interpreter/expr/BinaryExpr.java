
package interpreter.expr;

import interpreter.util.Utils;
import interpreter.value.NumberValue;
import interpreter.value.StringValue;
import interpreter.value.Value;
import interpreter.value.BooleanValue;

public class BinaryExpr extends Expr{
    
    private Expr left;
    private BinaryOp op;
    private Expr right;
    
    public BinaryExpr(int line, Expr left, BinaryOp op, Expr right){
        super(line);
        this.left = left;
        this.op = op;
        this.right = right;
    }
    
    @Override
    public Value<?> expr(){
        Value<?> v1 = left.expr();
        Value<?> v2 = right.expr();
        Value<?> ret = null;
        
        switch(op) {
            case And:
                ret = andOp();
		break;
            case Or:
                ret = orOp();
                break;
            case Equal:
            case NotEqual:
            case LowerThan:
            case LowerEqual:
            case GreaterThan:
            case GreaterEqual:   
                ret = relacionalOp(v1, v2, op);
                break;
            case Concat:
                ret = stringOp(v1, v2);
		break;
            case Add:
            case Sub:
            case Mul:
            case Div:
            case Mod:
                ret = numericOp(v1, v2, op);
                break;
        }
            
        
        return ret;
    }
    
    public Value<?> andOp() {
        Value<?> v = left.expr();
	Value<?> v2 = right.expr();
        
        if (v == null){
            return left.expr();
        }
        
        if (v instanceof BooleanValue) {
            BooleanValue bv = (BooleanValue) v;
            if (bv.value() == false)
            return left.expr();
        }       

        return v2;
    }
    
    public Value<?> orOp() {
        Value<?> v = left.expr();
        
        if (v == null){
            return right.expr();
        }
        
        if (v instanceof BooleanValue) {
            BooleanValue bv = (BooleanValue) v;
            if (bv.value() == false)
            return right.expr();
        }       

        return v;
    }
    
    public Value<?> relacionalOp(Value<?> v1, Value<?> v2, BinaryOp op){
        Value<?> ret = null;
        
        if ((v1 instanceof NumberValue && v2 instanceof NumberValue) || 
                (v1 instanceof StringValue && v2 instanceof StringValue)) {
            
            Double d1 = null;
            Double d2 = null;
            
            if(v1 instanceof NumberValue){
                NumberValue nv1 = (NumberValue) v1;
                d1 = nv1.value();
                NumberValue nv2 = (NumberValue) v2;
                d2 = nv2.value();
                
                switch(op) {
                    case Equal:
                        if(d1.equals(d2)){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case NotEqual:
                        if(d1.equals(d2)){
                            ret = new BooleanValue(false);
                        }
                        else {
                            ret = new BooleanValue(true);
                        }
                        break;
                    case LowerThan:
                        if(d1.doubleValue() < d2.doubleValue()){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case LowerEqual:
                        if(d1.doubleValue() <= d2.doubleValue()){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case GreaterThan:
                        if(d1.doubleValue() > d2.doubleValue()){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case GreaterEqual:
                        if(d1.doubleValue() >= d2.doubleValue()){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                }
            }
            else{
                StringValue sv1 = (StringValue) v1;
                String s1 = sv1.value();
                StringValue sv2 = (StringValue) v2;
                String s2 = sv2.value();
                
                switch(op) {
                    case Equal:
                        if(s1.equals(s2)){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case NotEqual:
                        if(s1.equals(s2)){
                            ret = new BooleanValue(false);
                        }
                        else {
                            ret = new BooleanValue(true);
                        }
                        break;
                    case LowerThan:
                        if(Double.valueOf(s1) < Double.valueOf(s2)){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case LowerEqual:
                        if(Double.valueOf(s1) <= Double.valueOf(s2)){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case GreaterThan:
                        if(Double.valueOf(s1) > Double.valueOf(s2)){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                    case GreaterEqual:
                        if(Double.valueOf(s1) >= Double.valueOf(s2)){
                            ret = new BooleanValue(true);
                        }
                        else {
                            ret = new BooleanValue(false);
                        }
                        break;
                }
            }
            
        } else {
            Utils.abort(super.getLine());
        }

        return ret;
    }
    
    private Value<?> stringOp(Value<?> v1, Value<?> v2){
        Value<?> ret = null;

        if ((v1 instanceof NumberValue || v1 instanceof StringValue) &&
                (v2 instanceof NumberValue || v2 instanceof StringValue)) {
            
            if(v1 instanceof NumberValue){
                NumberValue nv1 = (NumberValue) v1;
                Double d1 = nv1.value();

                if(v2 instanceof NumberValue){
                    NumberValue nv2 = (NumberValue) v2;
                    Double d2 = nv2.value();
                    
                    try {
                        String s1 = Double.toString(d1);
                        String s2 = Double.toString(d2);
                        ret = new StringValue(s1.concat(s2));
                    } catch (Exception e) {
                        Utils.abort(super.getLine());
                    }
                }
            
                else if(v2 instanceof StringValue){
                    StringValue sv2 = (StringValue) v2;
                    String s2 = sv2.value();
                    
                    try {
                        String s1 = Double.toString(d1);
                        ret = new StringValue(s1.concat(s2));
                    } catch (Exception e) {
                        Utils.abort(super.getLine());
                    }
 
                }
            }
            else if(v1 instanceof StringValue){
                StringValue sv1 = (StringValue) v1;
                String s1 = sv1.value();
                if(v2 instanceof NumberValue){
                    NumberValue nv2 = (NumberValue) v2;
                    Double d2 = nv2.value();
                    
                    try {
                        String s2 = Double.toString(d2);
                        ret = new StringValue(s1.concat(s2));
                    } catch (Exception e) {
                        Utils.abort(super.getLine());
                    }
                }
                else if(v2 instanceof StringValue){
                    StringValue sv2 = (StringValue) v2;
                    String s2 = sv2.value();
                    ret = new StringValue(s1.concat(s2));
                }
            }
        }

        return ret;
    }
    
    private Value<?> numericOp(Value<?> v1, Value<?> v2, BinaryOp op){
        Value<?> ret = null;
        
        if ((v1 instanceof NumberValue || v1 instanceof StringValue) &&
                (v2 instanceof NumberValue || v2 instanceof StringValue)) {
            
            Double d1 = null;
            Double d2 = null;
            
            if(v1 instanceof NumberValue){
                NumberValue nv1 = (NumberValue) v1;
                d1 = nv1.value();
            	
                if(v2 instanceof NumberValue){
                    NumberValue nv2 = (NumberValue) v2;
                    d2 = nv2.value();
                }
            
                else if(v2 instanceof StringValue){
                    StringValue sv2 = (StringValue) v2;
                    String s2 = sv2.value();
                    try {
                        d2 = Double.valueOf(s2);   
                    } catch (Exception e) {
                        Utils.abort(super.getLine());
                    }
                }
            }
            else if(v1 instanceof StringValue){
                StringValue sv1 = (StringValue) v1;
                String s1 = sv1.value();
                
                try {
                    d1 = Double.valueOf(s1);   
                } catch (Exception e) {
                    Utils.abort(super.getLine());
                }
            
                if(v2 instanceof NumberValue){
                    NumberValue nv2 = (NumberValue) v2;
                    d2 = nv2.value();
                }
                else if(v2 instanceof StringValue){
                    StringValue sv2 = (StringValue) v2;
                    String s2 = sv2.value();
                
                    try {
                        d2 = Double.valueOf(s2);   
                    } catch (Exception e) {
                        Utils.abort(super.getLine());
                    }
                }
            }
            
            switch(op){
                case Add:
                    ret = new NumberValue(d1+d2);
                    break;
                case Sub:
                    ret = new NumberValue(d1-d2);
                    break;
                case Mul:
                    ret = new NumberValue(d1*d2);
                    break;
                case Div:
                    ret = new NumberValue(d1/d2);
                    break;
                case Mod:
                    ret = new NumberValue(d1%d2);
                    break;
            }
            
        } else {
            Utils.abort(super.getLine());
        }
        
        return ret;
    }

    
}

