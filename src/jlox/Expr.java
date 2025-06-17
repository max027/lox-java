package jlox;
/* Grammer for
expression     → literal
               | unary
               | binary
               | grouping ;

literal        → NUMBER | STRING | "true" | "false" | "nil" ;
grouping       → "(" expression ")" ;
unary          → ( "-" | "!" ) expression ;
binary         → expression operator expression ;
operator       → "==" | "!=" | "<" | "<=" | ">" | ">="
               | "+"  | "-"  | "*" | "/" ;

 */
abstract class Expr {
    static class Binary extends Expr{
        final Expr left;
        final Expr right;
        final Token operator;
        Binary(Expr left,Token operator,Expr right){
            this.left=left;
            this.right=right;
            this.operator=operator;
        }
    }

}
