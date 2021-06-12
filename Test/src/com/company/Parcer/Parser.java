package com.company.Parcer;

import com.company.Lexer.Token;

import java.util.ArrayList;

public class Parser {

    private ArrayList<Token> tokens;

    private String tokenss="";

    private boolean duplicated = false;

    private Node root;

    public Parser(){

    }

    public void createAST(ArrayList<Token> tok) throws Exception {

        tokens = new ArrayList<>(tok);
        root = lang();

    }

    private Node lang() throws Exception {

        Node node = new Node("lang");

        while (tokens.size() > 0 && currTokenType().matches("VAR|IF|WHILE")) {
            node.addChild(expr());
        }

        //if(!lexemes.isEmpty()){throw new Exception("Error in lang");}

        Poliz.endPoliz();

        return node;

    }

    private Node expr() throws Exception {

        Node node = new Node("expr");

        switch (currTokenType()) {
            case ("VAR") -> node.addChild(expr_head());
            case ("IF") -> node.addChild(if_expr());
            case ("WHILE") -> node.addChild(while_expr());
            default -> throw new Exception("Error in expr");
        }

        return node;

    }

    private Node expr_head() throws Exception {

        Node node = new Node("expr_head");

        match("VAR", node);

        switch (currTokenType()) {
            case ("DEC"), ("INC") -> node.addChild(unary_operation());
            case ("ASSIGN_OP"),("TYPE_W") -> node.addChild(assign_expr());
            case ("METHOD") -> node.addChild(call_method());
            default -> throw new Exception("Error in expression head");
        }

        return node;

    }

    private Node unary_operation() throws Exception {

        Node node = new Node("unary_operation");

        switch (currTokenType()) {
            case ("DEC") -> match("DEC", node);
            case ("INC") -> match("INC", node);
            default -> throw new Exception("Error in unary_operation");
        }

        matchSemicolon();

        return node;

    }

    //Method
    private Node call_method() throws Exception {

        Node node = new Node("call_method");

        match("METHOD", node);

        node.addChild(method());

        return node;

    }

    private Node method() throws Exception {

        Node node = new Node("method");

        match("FUNC_OP", node);

        match("LB", node);
        node.addChild(inner_expression());
        match("RB", node);

        matchSemicolon();

        return node;

    }

    private Node inner_expression() throws Exception {

        Node node = new Node("inner_expression");

        switch (currTokenType()) {

            case ("NUM"), ("VAR") -> node.addChild(inner_value());

            case ("LB") -> {
                match("LB", node);
                node.addChild(inner_expression());
                match("RB", node);
            }

            default -> throw new Exception("Error in inner_expr");

        }

        while (currTokenType().matches("OP")) {

            match("OP", node);
            node.addChild(inner_expression());

        }

        return node;

    }

    private Node inner_value() throws Exception {

        Node node = new Node("inner_value");

        switch (currTokenType()) {
            case ("NUM") -> match("NUM", node);
            case ("VAR") -> match("VAR", node);
            default -> throw new Exception("Error in value");
        }

        return node;

    }

    //Default expr

    private Node assign_expr() throws Exception {

        Node node = new Node("assign_expr");

        switch (currTokenType()) {
            case ("ASSIGN_OP") -> {match("ASSIGN_OP", node); node.addChild(value_expr());}
            case ("TYPE_W") -> {match("TYPE_W", node); node.addChild(assign_struct());}
            default -> throw new Exception("Error in assign expression");
        }

        return node;

    }

    private Node value_expr() throws Exception {

        Node node = new Node("value_expr");

        switch (currTokenType()) {

            case ("NUM"), ("VAR") -> node.addChild(value());

            case ("LB") -> {
                match("LB", node);
                node.addChild(value_expr());
                match("RB", node);
            }

            default -> throw new Exception("Error in value_expr");

        }

        while (currTokenType().matches("OP")) {

            match("OP", node);
            node.addChild(value_expr());

        }

        if(!duplicated)matchSemicolon();
        duplicated=false;

        return node;

    }

    private Node value() throws Exception {

        Node node = new Node("value");

        switch (currTokenType()) {
            case ("NUM") -> match("NUM", node);
            case ("VAR") -> match("VAR", node);
            default -> throw new Exception("Error in value");
        }

        switch (currTokenType()) {
            case ("METHOD") -> {node.addChild(call_method()); duplicated=true;}
        }

        return node;

    }

    private Node assign_struct() throws Exception{

        Node node = new Node("assign_struct");

        match("TYPE", node);
        matchSemicolon();

        return node;

    }

    private Node if_expr() throws Exception {

        Node node = new Node("if_expr");

        node.addChild(if_head());
        node.addChild(if_else_body());

        if (currTokenType().matches("ELSE")) {
            node.addChild(else_expr());
        }

        return node;

    }

    private Node if_head() throws Exception {

        Node node = new Node("if_head");

        match("IF", node);
        node.addChild(if_condition());

        return node;

    }

    private Node else_expr() throws Exception {

        Node node = new Node("else_expr");

        match("ELSE_KW", node);
        node.addChild(if_else_body());

        return node;

    }

    private Node if_condition() throws Exception {

        Node node = new Node("if_condition");

        match("LB", node);
        node.addChild(logical_expr());
        match("RB", node);

        return node;

    }

    private Node logical_expr() throws Exception {

        Node node = new Node("logical_expr");

        switch (currTokenType()){

            case ("NUM"), ("VAR") -> node.addChild(inner_expression());
            default -> throw new Exception("Error in logical expr");

        }

        while (currTokenType().matches("BOOL_OP")) {
            match("BOOL_OP", node);
            node.addChild(inner_expression());
        }

        return node;

    }

    private Node if_else_body() throws Exception{

        Node node = new Node("ifelse_body");

        match("LCB", node);

        node.addChild(expr());

        while (currTokenType().matches("VAR|IF|WHILE")) node.addChild(expr());

        match("RCB", node);

        return node;

    }

    private Node while_expr() throws Exception{

        Node node = new Node("while_expr");

        node.addChild(while_head());
        node.addChild(while_body());

        return node;

    }

    private Node while_head() throws Exception{

        Node node = new Node("while_head");

        match("WHILE", node);
        node.addChild(while_condition());

        return node;

    }

    private Node while_condition() throws Exception{

        Node node = new Node("while_condition");

        match("LB", node);
        node.addChild(logical_expr());
        match("RB", node);

        return node;

    }

    private Node while_body() throws Exception{

        Node node = new Node("while_body");

        match("LCB", node);

        //node.addChild(expr());

        while (currTokenType().matches("VAR|IF|WHILE")) node.addChild(expr());

        match("RCB", node);

        return node;

    }

    //Tools

    private String currTokenType() {

        if (!tokens.isEmpty()){
            //System.out.println("token type is "+tokens.get(0).toString());
            return tokens.get(0).getType();
        }else {return "";}

    }

    private String currToken() {

        if (!tokens.isEmpty()){
            //Poliz
            //System.out.println("token type is "+tokens.get(0).toString());
            return tokens.get(0).getData();
        }else {return "";}

    }

    private Token currTokenn() {

        if (!tokens.isEmpty()){
            //System.out.println("token type is "+tokens.get(0).toString());
            return tokens.get(0);
        }else {return null;}

    }

    private void match(String token, Node currNode) {

        String t = currTokenType();

        tokenss += currToken()+" ";

        assert (t.equals(token)) : "Current Token != " + token;

        currNode.addToken(tokens.get(0));

        Poliz.makePolizPerToken(currTokenn());

        tokens.remove(0);

    }

    private void matchSemicolon() {

        String t = currTokenType();

        if(t.equals("C_OP"))tokenss += currToken()+" ";

        assert (t.equals("C_OP")) : "Semicolon missing";

        if(t.equals("C_OP")){
            Poliz.makePolizPerToken(currTokenn());
            tokens.remove(0);
        }

    }

    public void CheckTokens(){
        System.out.println("token line: "+tokenss);
    }

    public void print(){
        System.out.println("\n"+"Abstract Syntax Tree"+"\n");
        print(root, 0);
    }

    private void print(Node root, int level){

        String tab = "";
        for (int i = 0; i< level; i++){tab += "    ";}

        System.out.println(tab+root.getName());


        for (Token l: root.getTokens()) {
            System.out.println(tab+"->"+l.getData());
        }

        for (Node n: root.getChild()) {
            print(n, level+1);
        }

    }

}

class Node {

    private String name;

    private ArrayList<Node> children = new ArrayList<>();

    private ArrayList<Token> tokens = new ArrayList<>();


    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }



    public void addChild(Node node) { children.add(node); }

    public ArrayList<Node> getChild() {
        return children;
    }


    public void addToken(Token token) {
        tokens.add(token);
    }

    public ArrayList<Token> getTokens(){return tokens;}


}
