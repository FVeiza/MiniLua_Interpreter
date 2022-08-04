package syntatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import interpreter.command.AssignCommand;
import interpreter.command.BlocksCommand;
import interpreter.command.Command;
import interpreter.command.GenericForCommand;
import interpreter.command.NumericForCommand;
import interpreter.command.IfCommand;
import interpreter.command.RepeatCommand;
import interpreter.command.PrintCommand;
import interpreter.command.WhileCommand;
import interpreter.expr.AccessExpr;
import interpreter.expr.BinaryExpr;
import interpreter.expr.BinaryOp;
import interpreter.expr.ConstExpr;
import interpreter.expr.Expr;
import interpreter.expr.SetExpr;
import interpreter.expr.TableExpr;
import interpreter.expr.TableEntry;
import interpreter.expr.UnaryOp;
import interpreter.expr.UnaryExpr;
import interpreter.expr.Variable;
import interpreter.value.BooleanValue;
import interpreter.value.NumberValue;
import interpreter.value.StringValue;
import interpreter.value.Value;
import lexical.Lexeme;
import lexical.LexicalAnalysis;
import lexical.TokenType;

public class SyntaticAnalysis {

    private LexicalAnalysis lex;
    private Lexeme current;

    public SyntaticAnalysis(LexicalAnalysis lex) {
        this.lex = lex;
        this.current = lex.nextToken();
    }

    public Command start() {
        BlocksCommand bc = procCode();
        eat(TokenType.END_OF_FILE);
        
        return bc;
    }

    private void advance() {
        // System.out.println("Advanced (\"" + current.token + "\", " +
        //     current.type + ")");
        current = lex.nextToken();
    }

    private void eat(TokenType type) {
        // System.out.println("Expected (..., " + type + "), found (\"" + 
        //     current.token + "\", " + current.type + ")");
        if (type == current.type) {
            current = lex.nextToken();
        } else {
            showError();
        }
    }

    private void showError() {
        System.out.printf("%02d: ", lex.getLine());

        switch (current.type) {
            case INVALID_TOKEN:
                System.out.printf("Lexema inválido [%s]\n", current.token);
                break;
            case UNEXPECTED_EOF:
            case END_OF_FILE:
                System.out.printf("Fim de arquivo inesperado\n");
                break;
            default:
                System.out.printf("Lexema não esperado [%s]\n", current.token);
                break;
        }

        System.exit(1);
    }
    
    // <code> ::= { <cmd> }
    private BlocksCommand procCode(){
        int line = lex.getLine();
        List<Command> cmds = new ArrayList<Command>();
        while(current.type == TokenType.IF ||
                current.type == TokenType.WHILE ||
                current.type == TokenType.REPEAT ||
                current.type == TokenType.FOR ||
                current.type == TokenType.PRINT ||
                current.type == TokenType.ID){
            Command cmd = procCmd();
            cmds.add(cmd);
        }
        BlocksCommand bc = new BlocksCommand(line, cmds);
        
        return bc;
    }
    
    // <cmd> ::= (<if> | <while> | <repeat> | <for> | <print> | <assign>) [';']
    private Command procCmd(){
        Command cmd = null;
        if(current.type == TokenType.IF){
            cmd = procIf();
        }
        
        else if(current.type == TokenType.WHILE){
            cmd = procWhile();
        }
        
        else if(current.type == TokenType.REPEAT){
            cmd = procRepeat();
        }
        
        else if(current.type == TokenType.FOR){
            cmd = procFor();
        }
            
        else if(current.type == TokenType.PRINT){
            cmd = procPrint();
        }
        
        else if(current.type == TokenType.ID){
            cmd = procAssign();
        }
        
        else{
            showError();
        }
        
        if(current.type == TokenType.SEMI_COLON){
            advance();
        }
        
        return cmd;
    }
    
    // <if> ::= if <expr> then <code> { elseif <expr> then <code> } [ else <code> ] end             
    private IfCommand procIf(){                     
        BlocksCommand bc = null;
        eat(TokenType.IF);
        int line = lex.getLine();
        Expr expr = procExpr();//
        eat(TokenType.THEN);
        bc = procCode();
        
        IfCommand ic = new IfCommand(line, expr, bc);
        
        Vector<IfCommand> ifvector = new Vector<>();
        
        int contador = 0;
                
        while(current.type == TokenType.ELSEIF){
            advance();
            int elseLine = lex.getLine();
            Expr elseExpr = procExpr();
            eat(TokenType.THEN);
            
            BlocksCommand elseBC = null;
            elseBC = procCode();
            
            IfCommand newif = new IfCommand(elseLine, elseExpr, elseBC);
            
            ifvector.add(newif);
            
            if(ifvector.size() > 1){
                ifvector.get(contador-1).setElseCommands(ifvector.lastElement());
            }
            
            contador++;
        }
        
        Command elseCmds = null;
        
        if(current.type == TokenType.ELSE){
            advance();
            elseCmds = procCode();
        }
        
        if(!(ifvector.isEmpty())){
            ifvector.lastElement().setElseCommands(elseCmds);
            ic.setElseCommands(ifvector.firstElement());
        } else {
            if(elseCmds != null){
                ic.setElseCommands(elseCmds);
            }
        }
        
        eat(TokenType.END);
        
        return ic;
    }
    
    // <while> ::= while <expr> do <code> end           //Não finalizado
    private WhileCommand procWhile(){
        eat(TokenType.WHILE);
        int line = lex.getLine();
        Expr expr = procExpr();
        eat(TokenType.DO);
        Command cmd = procCode();
        eat(TokenType.END);
        
        WhileCommand wc = new WhileCommand(line, expr, cmd);
        
        return wc;
    }
    
    // <repeat> ::= repeat <code> until <expr>          //Não finalizado
    private RepeatCommand procRepeat(){
        eat(TokenType.REPEAT);
        int line = lex.getLine();
        BlocksCommand cmds = procCode();
        eat(TokenType.UNTIL);
        Expr expr = procExpr();
        
        RepeatCommand rc = new RepeatCommand(line, cmds ,expr);
        
        return rc;
    }
    
    // <for> ::= for <name> (('=' <expr> ',' <expr> [',' <expr>]) | ([',' <name>] in <expr>)) do <code> end         
    private Command procFor(){           
        Variable v1 = null;
        Variable v2 = null;
        Expr expr1 = null;
        Expr expr2 = null;
        Expr expr3 = null;
        int line = lex.getLine();
        
        eat(TokenType.FOR);
        v1 = procName();
        
        if(current.type == TokenType.ASSIGN){
            advance();
            expr1 = procExpr();
            eat(TokenType.COLON);
            expr2 = procExpr();
            if(current.type == TokenType.COLON){
                advance();
                expr3 = procExpr();
            }

        }
        
        else {
            if(current.type == TokenType.COLON){
                advance();
                v2 = procName();
            }
            
            eat(TokenType.IN);
            expr1 = procExpr();
            
            eat(TokenType.DO);
            BlocksCommand cmds = procCode();
            eat(TokenType.END);
            
            GenericForCommand gfc = new GenericForCommand(line, v1, v2, expr1, cmds);
            
            return gfc;
        }
        
        eat(TokenType.DO);
        BlocksCommand cmds = procCode();
        eat(TokenType.END);
        
        NumericForCommand nfc = new NumericForCommand(line, v1, expr1, expr2, expr3, cmds);
        
        return nfc;
    }
    
    // <print> ::= print '(' [ <expr> ] ')'
    private PrintCommand procPrint(){
        eat(TokenType.PRINT);
        eat(TokenType.OPEN_PAR);
        Expr expr = null;
        
        if(current.type == TokenType.OPEN_PAR ||
                current.type == TokenType.SUB ||
                current.type == TokenType.SIZE ||
                current.type == TokenType.NOT ||
                current.type == TokenType.NUMBER ||
                current.type == TokenType.STRING ||
                current.type == TokenType.FALSE ||
                current.type == TokenType.TRUE ||
                current.type == TokenType.NIL ||
                current.type == TokenType.READ ||
                current.type == TokenType.TONUMBER ||
                current.type == TokenType.TOSTRING ||
                current.type == TokenType.OPEN_CUR ||
                current.type == TokenType.ID){
            expr = procExpr();
        }
        int line = lex.getLine();
        eat(TokenType.CLOSE_PAR);
        
        PrintCommand pc = new PrintCommand(line, expr);
        
        return pc;
    }
    
    // <assign> ::= <lvalue> { ',' <lvalue> } '=' <expr> { ',' <expr> }         
    private AssignCommand procAssign(){
        
        Vector<SetExpr> lhs = new Vector<>();
        Vector<Expr> rhs = new Vector<>();
        lhs.add(procLValue());
        while(current.type == TokenType.COLON){
            advance();
            lhs.add(procLValue());
        }
        
        eat(TokenType.ASSIGN);
        int line = lex.getLine();
        rhs.add(procExpr());
        
        while(current.type == TokenType.COLON){
            advance();
            rhs.add(procExpr());
        }
        
        AssignCommand ac = new AssignCommand(line, lhs, rhs);
        
        return ac;
    }
    
    // <expr> ::= <rel> { (and | or) <rel> }                               
    private Expr procExpr(){
        Expr expr = procRel();
        int line = lex.getLine();
        Expr expr2 = null;
        BinaryOp op = null;
        
        while(current.type == TokenType.AND || current.type == TokenType.OR){
            
            if(current.type == TokenType.AND){
                op = BinaryOp.And;
            } 
            else if(current.type == TokenType.OR){
                op = BinaryOp.Or;
            }
            
            advance();
            expr2 = procRel();
            
            expr = new BinaryExpr(line, expr, op, expr2);
        }
        
        return expr;
    }
    
    // <rel> ::= <concat> [ ('<' | '>' | '<=' | '>=' | '~=' | '==') <concat> ]          
    private Expr procRel(){
        Expr expr = procConcat();
        int line = lex.getLine();
        Expr expr2 = null;
        BinaryOp op = null;
        
        if(current.type == TokenType.LOWER_THAN ||
                current.type == TokenType.GREATER_THAN ||
                current.type == TokenType.LOWER_EQUAL ||
                current.type == TokenType.GREATER_EQUAL ||
                current.type == TokenType.NOT_EQUAL ||
                current.type == TokenType.EQUAL){
            
            if(current.type == TokenType.LOWER_THAN){
                op = BinaryOp.LowerThan;
            } 
            else if(current.type == TokenType.GREATER_THAN){
                op = BinaryOp.GreaterThan;
            }
            else if(current.type == TokenType.LOWER_EQUAL){
                op = BinaryOp.LowerEqual;
            }
            else if(current.type == TokenType.GREATER_EQUAL){
                op = BinaryOp.GreaterEqual;
            }
            else if(current.type == TokenType.NOT_EQUAL){
                op = BinaryOp.NotEqual;
            }
            else if(current.type == TokenType.EQUAL){
                op = BinaryOp.Equal;
            }
            
            advance();
            expr2 = procConcat();
            
            expr = new BinaryExpr(line, expr, op, expr2);
        }
        
        return expr;
    }
    
    // <concat> ::= <arith> { '..' <arith> }               
    private Expr procConcat(){
        Expr expr = procArith();
        int line = lex.getLine();
        Expr expr2 = null;
        
        while(current.type == TokenType.CONCAT){
            advance();
            expr2 = procArith();
            
            expr = new BinaryExpr(line, expr, BinaryOp.Concat, expr2);
        }

        return expr;
    }
    
    // <arith> ::= <term> { ('+' | '-') <term> }                
    private Expr procArith(){
        Expr expr = procTerm();
        int line = lex.getLine();
        Expr expr2 = null;
        BinaryOp op = null;
        
        while(current.type == TokenType.ADD || current.type == TokenType.SUB){
            
            if(current.type == TokenType.ADD){
                op = BinaryOp.Add;
		advance();
            } 
            else if(current.type == TokenType.SUB){
                op = BinaryOp.Sub;
		advance();
            }
	    
            expr2 = procTerm();
                        
            expr = new BinaryExpr(line, expr, op, expr2);

        }
        
        return expr;
    }
    
    // <term> ::= <factor> { ('*' | '/' | '%') <factor> }           
    private Expr procTerm(){
        Expr expr = procFactor();
        int line = lex.getLine();
        Expr expr2 = null;
        BinaryOp op = null;
        
        while(current.type == TokenType.MUL || current.type == TokenType.DIV || current.type == TokenType.MOD){
   
            if(current.type == TokenType.MUL){
                op = BinaryOp.Mul;
            } 
            else if(current.type == TokenType.DIV){
                op = BinaryOp.Div;
            }
            else if(current.type == TokenType.MOD){
                op = BinaryOp.Mod;
            }
            
            advance();
            expr2 = procFactor();
                       
            expr = new BinaryExpr(line, expr, op, expr2);

        }
        
        return expr;
    }
    
    // <factor> ::= '(' <expr> ')' | [ '-' | '#' | not] <rvalue>        
    private Expr procFactor(){
        Expr expr = null;
        if(current.type == TokenType.OPEN_PAR){
            advance();
            expr = procExpr();
            eat(TokenType.CLOSE_PAR);
        }
        
        else{
            UnaryOp op = null;
            if(current.type == TokenType.SUB || current.type == TokenType.SIZE || current.type == TokenType.NOT){
                
                if(current.type == TokenType.SUB){
                    advance();
                    op = UnaryOp.Neg;
                }
                else if(current.type == TokenType.SIZE){
                    advance();
                    op = UnaryOp.Size;
                }

                else if(current.type == TokenType.NOT){
                    advance();
                    op = UnaryOp.Not;
                }    
            }
            int line = lex.getLine();
            expr = procRValue();

            if(op != null){
                expr = new UnaryExpr(line, expr, op);
            }
        }
        
        return expr;
    }
    
    // <lvalue> ::= <name> { '.' <name> | '[' <expr> ']' }      
    private SetExpr procLValue(){
        Variable var = procName();
        
        Variable var2 = null;
        SetExpr ret = null;
        Expr expr = null;
        
        if(current.type == TokenType.DOT || current.type == TokenType.OPEN_BRA){
            Expr aux = var;
            while(current.type == TokenType.DOT || current.type == TokenType.OPEN_BRA){
                if(current.type == TokenType.DOT){
                    advance();
                    var2 = procName();
                
                    int line = lex.getLine();
                    String name2 = var2.getName();
                    StringValue sv2 = new StringValue(name2);
                
                    ConstExpr ce2 = new ConstExpr(line, sv2);
                    ret = new AccessExpr(line, aux, ce2);
                    aux = ret;
                }
            
                else{
                    eat(TokenType.OPEN_BRA);
                    expr = procExpr();
                    int line = lex.getLine();
                    eat(TokenType.CLOSE_BRA);
                    ret = new AccessExpr(line, aux, expr);
                    aux = ret;
                }
            }
        }
        else{
            ret = var;
        }
  
        return ret;
    }
    
    // <rvalue> ::= <const> | <function> | <table> | <lvalue>       
    private Expr procRValue(){
        Expr expr = null;
        if(current.type == TokenType.NUMBER || 
                current.type == TokenType.STRING || 
                current.type == TokenType.FALSE || 
                current.type == TokenType.TRUE ||
                current.type == TokenType.NIL){
            Value<?> v = procConst();
            int line = lex.getLine();
            expr = new ConstExpr(line, v);
        }
        
        else if(current.type == TokenType.READ || 
                current.type == TokenType.TONUMBER || 
                current.type == TokenType.TOSTRING){
            expr = procFunction();
        }
        
        else if(current.type == TokenType.OPEN_CUR){
            expr = procTable();
        }
        
        else{
            expr = procLValue();
        }
        
        return expr;
    }
    
    // <const> ::= <number> | <string> | false | true | nil
    private Value<?> procConst(){
        Value<?> v = null;
        if(current.type == TokenType.NUMBER){
            v = procNumber();
        }
        
        else if(current.type == TokenType.STRING){
            v = procString();
        }
        
        else if(current.type == TokenType.FALSE){
            v = new BooleanValue(false);
            advance();
        }
        
        else if(current.type == TokenType.TRUE){
            v = new BooleanValue(true);
            advance();
        }
        
        else if(current.type == TokenType.NIL){
            v = null;
            advance();
        }
        
        else{
            showError();
        }
        
        return v;
    }
    
    // <function> ::= (read | tonumber | tostring) '(' [ <expr> ] ')'
    private UnaryExpr procFunction(){
        Expr expr = null;
        UnaryOp op = null;
        
        if(current.type == TokenType.READ ){
            advance();
            op = UnaryOp.Read;
        }
        else if(current.type == TokenType.TONUMBER){
            advance();
            op = UnaryOp.ToNumber;
        }
        else if(current.type == TokenType.TOSTRING){
            advance();
            op = UnaryOp.ToString;
        }
        
        int line = lex.getLine();
        eat(TokenType.OPEN_PAR);
        
        
        if(current.type == TokenType.OPEN_PAR ||
                current.type == TokenType.SUB ||
                current.type == TokenType.SIZE ||
                current.type == TokenType.NOT ||
                current.type == TokenType.NUMBER ||
                current.type == TokenType.STRING ||
                current.type == TokenType.FALSE ||
                current.type == TokenType.TRUE ||
                current.type == TokenType.NIL ||
                current.type == TokenType.READ ||
                current.type == TokenType.TONUMBER ||
                current.type == TokenType.TOSTRING ||
                current.type == TokenType.OPEN_CUR ||
                current.type == TokenType.ID){
            expr = procExpr();
        }    
        
        eat(TokenType.CLOSE_PAR);
        
        UnaryExpr ue = new UnaryExpr(line, expr, op);
        
        return ue;
    }
    
    // <table> ::= '{' [ <elem> { ',' <elem> } ] '}'      
    private TableExpr procTable(){
        eat(TokenType.OPEN_CUR);
        int line = lex.getLine();
        
        TableExpr te = new TableExpr(line);
        
        TableEntry ten = null;
        
        if(current.type == TokenType.OPEN_BRA || 
		current.type == TokenType.OPEN_PAR ||
                current.type == TokenType.SUB ||
                current.type == TokenType.SIZE ||
                current.type == TokenType.NOT ||
                current.type == TokenType.NUMBER ||
                current.type == TokenType.STRING ||
                current.type == TokenType.FALSE ||
                current.type == TokenType.TRUE ||
                current.type == TokenType.NIL ||
                current.type == TokenType.READ ||
                current.type == TokenType.TONUMBER ||
                current.type == TokenType.TOSTRING ||
                current.type == TokenType.OPEN_CUR ||
                current.type == TokenType.ID){

            ten = procElem(); 
            te.addEntry(ten);
            
            while(current.type == TokenType.COLON){
                advance();
                ten = procElem();
                te.addEntry(ten);
            }
        }
        
        eat(TokenType.CLOSE_CUR);
        
        return te;
    }
    
    // <elem> ::= [ '[' <expr> ']' '=' ] <expr>         
    private TableEntry procElem(){
        Expr exkey = null;
        Expr exvalue = null;
        if(current.type == TokenType.OPEN_BRA){
            advance();
            exkey = procExpr();
            eat(TokenType.CLOSE_BRA);
            eat(TokenType.ASSIGN);
        }
        else{
            int line = lex.getLine();
            StringValue sv = new StringValue("");
            ConstExpr ce = new ConstExpr(line, sv);
            exkey = null;
            
        }
        exvalue = procExpr();
        TableEntry te = new TableEntry(exkey, exvalue);
        
        return te;
    }
    
    private Variable procName(){
        String name = current.token;
        eat(TokenType.ID);
        int line = lex.getLine();
        Variable var = new Variable(line, name);
        
        return var;
    }
    
    private NumberValue procNumber(){
        String tmp = current.token;
        eat(TokenType.NUMBER);
        
        Double v = Double.valueOf(tmp);
        NumberValue nv = new NumberValue(v);
        
        return nv;
    }
    
    private StringValue procString(){
        String name = current.token;
        eat(TokenType.STRING);
        
        StringValue sv = new StringValue(name);
        return sv;
    }
}
