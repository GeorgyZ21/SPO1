package com.company;

import com.company.Lexer.Lexer;
import com.company.Lexer.Token;
import com.company.Parcer.Parser;
import com.company.Parcer.Poliz;
import com.company.Stack_Maschine.PolizCalculation;

import java.util.ArrayList;
import java.util.LinkedList;

public class Main {
//c = 1; i = 0; a new List; a.add(5+5); a.add(10); a.add(5); b new Set; b.add(3); b.add(3); c = a.get(1); while(i < 5){i = i + 1;} c++; c++;
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        LinkedList<Token> tokens = lexer.lex("c = 1; i = 0; a new List; a.add(5+5); a.add(10); a.add(5); b new Set; b.add(3); b.add(3); c = a.get(1); while(i < 5){i = i + 1;}" );
        Poliz.setTokens(tokens);
        System.out.println("\n"+"Токены:");
        for (int i = 0; i < tokens.size(); i++)
        {
            System.out.println(tokens.get(i));
        }
        Parser parser = new Parser();
        try {
            parser.createAST(new ArrayList<>(tokens));
            parser.CheckTokens();
            parser.print();
        }catch ( Exception ex)
        { System.err.println(ex);
            System.exit(1);
        }


        int j = 0;
        for (Token token : Poliz.poliz) {
            System.out.println(j + " " + token);
            j++;
        }

/*
        System.out.println("\n"+"ОПЗ:");
        LinkedList<Token> testPoliz = Poliz.makePoliz(tokens);
        int i = 0;
        for (Token token : testPoliz) {
            System.out.println(i + " " + token);
            i++;
        }

 */

        System.out.println("\n"+"Таблица переменных:");
        PolizCalculation.calculate(Poliz.poliz);
        //PolizCalculation.calculate(testPoliz);


    }
}
