package com.company.Lexer;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static LinkedList<Token> lex(String input) {
        LinkedList<Token> tokens = new LinkedList<Token>();
        boolean flag; //флаг изменения type
        Lexem lexem = null; //текущее значение type
        String substr = ""; //текущее значение data
        int i = 0; //индекс перемещения по строке
        while (input.length() > i) {
            flag = false;
            substr += input.charAt(i); //считываем следующий символ
            for (Lexem l : Lexem.values()) { //определяем тип токена
                Pattern pattern = l.getPattern();
                Matcher matcher = pattern.matcher(substr);
                if (matcher.matches()) {
                    lexem = l;
                    flag = true; //если определили успешно, то изменяем текущее значение токена и идем дальше
                    break;
                }
            }
            if (input.charAt(i) == ' ' || input.charAt(i) == '\n' || input.charAt(i) == '\t') { //если пробел, перенос строки или табуляция, то игнорируем
                if (lexem != null) {
                    tokens.add(new Token(lexem, substr.substring(0, substr.length()-1)));
                    lexem = null;
                    substr = "";
                }
            }
            if (lexem != null && !flag) { //если тип токена не определился при считывании нового символа, то возвращаемся назад на 1 символ и добавляем токен
                substr = substr.substring(0, substr.length()-1);
                i--;
                tokens.add(new Token(lexem, substr));
                lexem = null;
                substr = "";
            }
            i++;
        }
        if (lexem != null)
        tokens.add(new Token(lexem, substr)); //добавляем последний токен
        return tokens;
    }
}
