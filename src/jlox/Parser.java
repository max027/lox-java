package jlox;
// Recursive-descent parser

import java.util.ArrayList;
import java.util.List;
import static  jlox.Tokentype.*;
public class Parser {
    private static class ParseError extends RuntimeException{

    }
    private final List<Token> tokens;
    private int current=0;

    Parser(List<Token> tokens){
        this.tokens=tokens;
    }
    List<Stmt> parse(){
        List<Stmt> statements=new ArrayList<>();
        while (!isAtEnd()){
            statements.add(statement());
        }
        return statements;
    }
    private Stmt statement(){
        if (match(PRINT)){
            return printStatement();
        }
        return expressionStatement();
    }

    private Stmt printStatement(){
        Expr value=expression();
        consume(SEMICOLON,"Expected ';' after value");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement(){
        Expr value=expression();
        consume(SEMICOLON,"Expected ';' after value");
        return new Stmt.Expression(value);
    }

    private Expr expression(){
        return equality();
    }
    //equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality(){
        Expr expr=comparison();
        while (match(BANG_EQUAL,EQUAL_EQUAL)){
            Token operator=previous();
            Expr right=comparison();
            expr=new Expr.Binary(expr,operator,right);
        }
        return expr;
    }


    private boolean match(Tokentype... types){
        for (Tokentype type:types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }
    private  Token consume(Tokentype type,String message){
        if(check(type)){
           return advance();
        }
        throw error(peek(),message);
    }
    private boolean check(Tokentype type){
        if (isAtEnd()){
            return false;
        }
        return peek().type==type;
    }
    private Token advance(){
        if(!isAtEnd()){
            current++;
        }
        return previous();
    }

    private boolean isAtEnd(){
        return peek().type==EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current-1);
    }
    private ParseError error(Token token,String message){
        jlox.error(token,message);
        return new ParseError();
    }
    private void synchronize(){
        advance();
        while (!isAtEnd()){
            if (previous().type==SEMICOLON){
                return;
            }
            switch (peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }
    //comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison(){
        Expr expr=term();
        while (match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)){
            Token operator=previous();
            Expr right=term();
            expr=new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    //term  → factor ( ( "-" | "+" ) factor )* ;
    private Expr term(){
        Expr expr=factor();
        while (match(MINUS,PLUS)){
            Token operator=previous();
            Expr right=factor();
            expr=new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

   //factor → unary ( ( "/" | "*" ) unary )* ;
    private Expr factor(){
        Expr expr=unary();
        while (match(SLASH,STAR)){
            Token operator=previous();
            Expr right=unary();
            expr=new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    //unary → ( "!" | "-" ) unary | primary ;
    private Expr unary(){
        if (match(BANG,MINUS)){
            Token operator=previous();
            Expr right=unary();
            return new Expr.Unary(operator,right);
        }
        return primary();
    }

    //primary  → NUMBER | STRING | "true" | "false" | "nil"  | "(" expression ")" ;
    private Expr primary(){
        if (match(FALSE)){
            return new Expr.Literal(FALSE);
        }
        if (match(TRUE)){
            return new Expr.Literal(TRUE);
        }
        if (match(NIL)){
            return new Expr.Literal(NIL);
        }

        if (match(NUMBER,STRING)){
            return new Expr.Literal(previous().literal);
        }
        if (match(LEFT_PAREN)){
            Expr expr=expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(),"Expected expression");
    }
}
