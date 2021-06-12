package com.company.Parcer;

import com.company.Lexer.Lexem;
import com.company.Lexer.Token;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Poliz {

    public static LinkedList<Token> poliz = new LinkedList<>();
    private static Stack<Token> stack = new Stack<>();
    public static Queue<Token> tokens;

    //Poliz per token
    public static void makePolizPerToken(Token token) {

            if (token.type == Lexem.WHILE) {
                stack.add(new Token(Lexem.GOTO, "!"));
                stack.add(new Token(Lexem.GOTO_INDEX, Integer.toString(poliz.size())));
                stack.add(new Token(Lexem.GOTO, "!F"));
                stack.add(new Token(Lexem.GOTO_INDEX, Integer.toString(p(poliz.size(), tokens))));
            }

            if (token.type == Lexem.IF) {
                stack.add(new Token(Lexem.GOTO, "!F"));
                stack.add(new Token(Lexem.GOTO_INDEX, Integer.toString(p(poliz.size(), tokens))));
            }

            if(token.type == Lexem.ELSE){
                poliz.add(new Token(Lexem.GOTO_INDEX, Integer.toString(p(poliz.size(), tokens))));
                poliz.add(new Token(Lexem.GOTO, "!"));
            }

            if (token.type == Lexem.TYPE_W) {
                System.out.println("new");
                stack.add(token);
            }

            if (token.type == Lexem.TYPE) {
                poliz.add(token);
                poliz.add(stack.pop());
            }

            //Token tk = tokens.poll();
            if (token.type == Lexem.INC || token.type == Lexem.DEC)
            {
                poliz.add(poliz.getLast());
                Token tmpToken = new Token(Lexem.NUM, "1");
                poliz.add(tmpToken);
                if (token.type == Lexem.INC)
                {
                    tmpToken = new Token(Lexem.OP, "+");
                }
                else {tmpToken = new Token(Lexem.OP, "-");}
                poliz.add(tmpToken);
                poliz.add(new Token(Lexem.ASSIGN_OP, "="));
            }

            //Если лексема является числом или переменной, добавляем ее в ПОЛИЗ-массив.
            if (token.type == Lexem.VAR || token.type == Lexem.NUM) {
                poliz.add(token);
            }

            //Если лексема является бинарной операцией, тогда:
            if (token.type == Lexem.OP || token.type == Lexem.BOOL_OP || token.type == Lexem.ASSIGN_OP || token.type == Lexem.FUNC_OP) {
                if (!stack.empty()) {
                    while (getPriorOfOp(token.data) >= getPriorOfOp(stack.peek().data)) {
                        if(stack.peek().type != Lexem.GOTO && stack.peek().type != Lexem.GOTO_INDEX)poliz.add(stack.pop());
                        if (stack.empty() || stack.peek().type == Lexem.GOTO || stack.peek().type == Lexem.GOTO_INDEX){
                            break;
                        }
                    }
                }
                stack.push(token);
            }

            //Если лексема является открывающей скобкой, помещаем ее в стек.
            if (token.type == Lexem.LB) {
                stack.push(token);
            }

            if (token.type == Lexem.RB) {
                if (!stack.empty()) {
                    while (!stack.empty() && stack.peek().type != Lexem.LB) {
                        poliz.add(stack.pop());
                    }
                    if (!stack.empty() && stack.peek().type == Lexem.LB) {
                        stack.pop();
                    }
                    if (!stack.empty() && (stack.peek().type == Lexem.GOTO||stack.peek().type == Lexem.GOTO_INDEX)) {
                        poliz.add(stack.pop());
                        poliz.add(stack.pop());
                    }
                }
            }

        if (token.type == Lexem.LCB) {
            //stack.push(token);
        }

        if (token.type == Lexem.RCB) {
            if (!stack.empty()) {
                while (!stack.empty() && stack.peek().type != Lexem.LCB) {
                    poliz.add(stack.pop());
                }
                if (!stack.empty() && stack.peek().type == Lexem.LCB) {
                    stack.pop();
                }
                if (!stack.empty() && (stack.peek().type == Lexem.GOTO||stack.peek().type == Lexem.GOTO_INDEX)) {
                    poliz.add(stack.pop());
                    poliz.add(stack.pop());
                }
            }
        }

            if (token.type == Lexem.C_OP) {
                while (!stack.empty()) {
                    poliz.add(stack.pop());
                }
            }

    }

    public static void endPoliz(){
        while (!stack.empty()) {
            poliz.add(stack.pop());
        }
    }

    public static void setTokens(Queue<Token> input){
        tokens = input;
    }

    //Poliz from Queue
    public static LinkedList<Token> makePoliz(Queue<Token> input) {
        while (!input.isEmpty()) {
            Token token = input.peek();
            if (!(token.type == Lexem.WHILE || token.type == Lexem.IF)) {
                makePolizFromExpr(input);
            }
            else {
                makePolizFromWhile(input, token);
            }
        }

        return poliz;
    }

    private static void makePolizFromWhile(Queue<Token> input, Token tmp) {
        Queue<Token> boolExpr = new LinkedList<>();
        input.poll();
        Token token = input.poll();
        int index = poliz.size();
        while (token.type != Lexem.LCB) {
            boolExpr.add(token);
            token = input.poll();
        }

        makePolizFromExpr(boolExpr);
        if (tmp.type == Lexem.WHILE) {
            poliz.add(new Token(Lexem.GOTO_INDEX, Integer.toString(p(poliz.size(), input))));
        }
        String p = Integer.toString(p(poliz.size(), input));
        if (tmp.type == Lexem.IF) {
            poliz.add(new Token(Lexem.GOTO_INDEX, p));
        }
        poliz.add(new Token(Lexem.GOTO, "!F"));

        Queue<Token> expr = new LinkedList<>();
        token = input.poll();
        while (token.type != Lexem.RCB) {
            if (token.type == Lexem.WHILE || token.type == Lexem.IF) {
                makePolizFromExpr(expr);
                makePolizFromWhile(input, token);
            }
            if (!(token.type == Lexem.WHILE || token.type == Lexem.IF))
                expr.add(token);
            token = input.poll();
        }
        makePolizFromExpr(expr);
        if (tmp.type != Lexem.IF)
            poliz.add(new Token(Lexem.GOTO_INDEX, Integer.toString(index)));
        if (tmp.type != Lexem.WHILE)
            poliz.add(new Token(Lexem.GOTO_INDEX, p));
        poliz.add(new Token(Lexem.GOTO, "!"));
    }

    private static int p(int size, Queue<Token> tokens) {
        int p = size;
        int i = 1;

        Queue<Token> newtokens = new LinkedList<>(tokens);
        Token newtoken = newtokens.poll();

        while (i > 0){
            if (newtokens.isEmpty())
            {
                break;
            }
            else
            {
                newtoken = newtokens.poll();
            }
            if (newtoken.type == Lexem.WHILE || newtoken.type == Lexem.IF) {
                i++;
                p--;
            }
            if (newtoken.type == Lexem.RCB) {
                i--;
            }
            if (newtoken.type != Lexem.C_OP) {
                if(!(newtoken.type == Lexem.INC || newtoken.type == Lexem.DEC)){
                p++;
                }
                else p = p + 4;
            }
        }
        //p+=4;

        return p;
    }

    private static void makePolizFromExpr(Queue<Token> input) {
        Stack<Token> stack = new Stack<>();

        while (!input.isEmpty()) {
            Token token = input.peek();

            if (token.type == Lexem.WHILE || token.type == Lexem.IF) {
                break;
            }

            if (token.type == Lexem.TYPE_W) {
                stack.add(token);
            }

            if (token.type == Lexem.TYPE) {
                poliz.add(token);
                poliz.add(stack.pop());
            }

            token = input.poll();
            if (token.type == Lexem.INC || token.type == Lexem.DEC)
            {
                poliz.add(poliz.getLast());
                Token tmpToken = new Token(Lexem.NUM, "1");
                poliz.add(tmpToken);
                if (token.type == Lexem.INC)
                {
                    tmpToken = new Token(Lexem.OP, "+");
                }
                else {tmpToken = new Token(Lexem.OP, "-");}
                poliz.add(tmpToken);
                poliz.add(new Token(Lexem.ASSIGN_OP, "="));
            }

            //Если лексема является числом или переменной, добавляем ее в ПОЛИЗ-массив.
            if (token.type == Lexem.VAR || token.type == Lexem.NUM) {
                poliz.add(token);
            }

            //Если лексема является бинарной операцией, тогда:
            if (token.type == Lexem.OP || token.type == Lexem.BOOL_OP || token.type == Lexem.ASSIGN_OP || token.type == Lexem.FUNC_OP) {
                if (!stack.empty()) {
                    while (getPriorOfOp(token.data) >= getPriorOfOp(stack.peek().data)) {
                        poliz.add(stack.pop());
                        if (stack.empty()){
                            break;
                        }
                    }
                }
                stack.push(token);
            }

            //Если лексема является открывающей скобкой, помещаем ее в стек.
            if (token.type == Lexem.LB) {
                stack.push(token);
            }

            if (token.type == Lexem.RB) {
                if (!stack.empty()) {
                    while (!stack.empty() && stack.peek().type != Lexem.LB) {
                        poliz.add(stack.pop());
                    }
                    if (!stack.empty() && stack.peek().type == Lexem.LB) {
                        stack.pop();
                    }
                }
            }

            if (token.type == Lexem.C_OP) {
                while (!stack.empty()) {
                    poliz.add(stack.pop());
                }
            }
        }

        while (!stack.empty()) {
            poliz.add(stack.pop());
        }
    }

    private static int getPriorOfOp(String op) {
        if (op.equals("*") || op.equals("/"))
            return 0;
        else if (op.equals("*") || op.equals("/"))
            return 1;
        else if (op.equals("+") || op.equals("-"))
            return 2;
        else if (op.equals(">") || op.equals(">=") || op.equals("<") || op.equals("<=") || op.equals("==") || op.equals("!="))
            return 3;
        else if (op.equals("="))
            return 5;
        else
            return 4;
    }

    public static LinkedList<Token> getPoliz(){return poliz;}

}
