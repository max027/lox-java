package jlox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>,Stmt.Visitor<Void> {
    private Environment environment=new Environment();
    void interpret(List<Stmt> statement){
            try {
               for (Stmt stmt:statement){
                   execute(stmt);
               }
            }catch (RuntimeError error){
                    jlox.runtimeError(error);
            }
    }


    private void execute(Stmt stmt){
        stmt.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        executeBlock(stmt.statements,new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements,Environment environment){
        Environment previous=this.environment;
       try{
           this.environment=environment;
           for (Stmt stmt:statements){
               execute(stmt);
           }
       }finally {
           this.environment=previous;
       }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // evaluate expression from left to right
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            //comparison operator
            case GREATER:
                checkNumberOperand(expr.operator,left,right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator,left,right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperand(expr.operator,left,right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator,left,right);
                return (double)left <= (double)right;
            // arthematic operator
            case MINUS:
                checkNumberOperand(expr.operator,left,right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperand(expr.operator,left,right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperand(expr.operator,left,right);
                return (double) left * (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double){
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String){
                    return (String) left + (String) right;
                }
               throw new RuntimeError(expr.operator,"Operand must be two numbers or two string");

        }
        // unreachable code
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }


        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    //nil and false are falsy and everything other is truthy
    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isEqual(Object a,Object b){
        if (a==null && b==null){
            return true;
        }
        if (a==null){
            return false;
        }
        return a.equals(b);
    }
    private String stringify(Object object){
        if (object==null){
            return "nil";
        }
        if (object instanceof Double){
            String text=object.toString();
            if (text.endsWith(".0")){
                text=text.substring(0,text.length()-2);
            }
            return text;
        }

        return object.toString();
    }
    private void checkNumberOperand(Token operator,Object operand){
        if (operand instanceof  Double){
            return;
        }
        throw new RuntimeError(operator,"operand must be a number.");
    }
    private void checkNumberOperand(Token operator,Object left,Object right){
        if (left instanceof  Double && right instanceof  Double){
            return;
        }
        throw new RuntimeError(operator,"Operand must be number");
    }



    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value=evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value=null;
        if (stmt.initializer!=null){
            value=evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme,value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value=evaluate(expr.value);
        environment.assign(expr.name,value);
        return value;
    }
}
