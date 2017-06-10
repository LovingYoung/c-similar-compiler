package jace.app.SyntacticAnalysis;

import jace.app.Tokenizer.Token;
import jace.app.Tokenizer.Tokenizer;

import java.util.Arrays;
import java.util.List;

/**
 * Created by jaceliu on 08/06/2017.
 */
public class SyntacticAnalyze {
    /**
     * Store generated tokens
     */
    private static List<Token> tokens;

    /**
     * The function to do syntactic analysis
     * @param code the code to be analyzed
     * @return the whole parse tree
     */
    public static ParseTreeNode analyze(String code){
        tokens = new Tokenizer(code).tokenize();
        ParseTreeNode root = new ParseTreeNode("", "");
        try {
            root = Program.process();
        } catch (ParseException e){
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            System.exit(-1);
        }
        return root;
    }

    /**
     * &lt;Program&gt; ::= &lt;DeclarativeChain&gt;
     */
    private static class Program{
        public static Token[] getFirst(){
            return DeclarativeChain.getFirst();
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Program", "");
            root.addChild(DeclarativeChain.process());
            return root;
        }
    }

    /**
     * &lt;Declarative&gt; ::= int &lt;Id&gt; &lt;DeclarativeType&gt; | void &lt;Id&gt; &lt;FunctionDeclarative&gt;
     */
    private static class Declarative {
        public static Token[] getFisrt() {
            Token[] tokens = new Token[]{new Token("int", Token.Type.KEYWORD), new Token("void", Token.Type.KEYWORD)};
            return tokens;
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Declarative", "");
            if(tokens.get(0).equals(new Token("int", Token.Type.KEYWORD))){
                tokens.remove(0);
                root.addChild(new ParseTreeNode("KEYWORD", "int"));
                root.addChild(Id.process());
                root.addChild(DeclarativeType.process());
            } else if(tokens.get(0).equals(new Token("void", Token.Type.KEYWORD))){
                tokens.remove(0);
                root.addChild(new ParseTreeNode("KEYWORD", "void"));
                root.addChild(Id.process());
                root.addChild(FunctionDeclarative.process());
            } else{
                throw new ParseException(tokens.get(0).getContent());
            }
            return root;
        }
    }

    /**
     * &lt;DeclarativeChain&gt; ::= &lt;Declarative&gt; {&lt;Declarative&gt;}
     */
    private static class DeclarativeChain {
        public static Token[] getFirst() {
            return Declarative.getFisrt();
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("DeclarativeChain", "");
            while(tokens.size() > 0) root.addChild(Declarative.process());
            return root;
        }
    }

    /**
     * &lt;Id&gt; ::= IDENTIFIER
     */
    private static class Id {
        public static Token[] getFirst(){
            return new Token[]{new Token("", Token.Type.IDENTIFIER)};
        }
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root;
            if(tokens.get(0).getType() == Token.Type.IDENTIFIER){
                root = new ParseTreeNode("IDENTIFIER", tokens.get(0).getContent());
                tokens.remove(0);
            } else {
                throw new ParseException(tokens.get(0).getContent());
            }
            return root;
        }
    }

    /**
     * &lt;DeclarativeType&gt; ::= &lt;VariablesDeclarative&gt; | &lt;FunctionDeclarative&gt;
     */
    private static class DeclarativeType {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("DeclarativeType", "");
            if(Arrays.asList(VariablesDeclarative.getFirst()).contains(tokens.get(0))){
                root.addChild(VariablesDeclarative.process());
            } else if(Arrays.asList(FunctionDeclarative.getFirst()).contains(tokens.get(0))){
                root.addChild(FunctionDeclarative.process());
            } else{
                throw new ParseException(tokens.get(0).getContent());
            }
            return root;
        }
    }

    /**
     * &lt;VariableDeclarative&gt; ::= ;
     */
    private static class VariablesDeclarative {
        public static Token[] getFirst() {
            return new Token[]{new Token(";", Token.Type.FIELD_OP)};
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root;
            Token token = tokens.get(0);
            tokens.remove(0);
            if(token.equals(new Token(";", Token.Type.FIELD_OP))){
                root = new ParseTreeNode("FIELD_OP", token.getContent());
            } else {
                throw new ParseException(token.getContent());
            }
            return root;
        }
    }

    /**
     * &lt;FunctionDeclarative&gt; ::= '(' &lt;FormalParamter&gt; ')' &lt;StatementBlock&gt;
     */
    private static class FunctionDeclarative {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("FunctionDeclarative", "");
            Token token = tokens.get(0);
            tokens.remove(0);
            if(!token.equals(new Token("(", Token.Type.LEFT_PARENTHESIS)))
                throw new ParseException(token.getContent());
            root.addChild(new ParseTreeNode("LEFT_PARENTHESIS", token.getContent()));
            root.addChild(FormalParameter.process());
            token = tokens.get(0);
            tokens.remove(0);
            if(!token.equals(new Token(")", Token.Type.RIGHT_PARENTHESIS)))
                throw new ParseException(token.getContent());
            root.addChild(new ParseTreeNode("RIGHT_PARENTHESIS", token.getContent()));
            root.addChild(StatementBlocks.process());
            return root;
        }

        public static Token[] getFirst() {
            return new Token[]{new Token("(", Token.Type.LEFT_PARENTHESIS)};
        }
    }

    /**
     * &lt;FormalParameter&gt; ::= &lt;ParameterList&gt; | void
     */
    private static class FormalParameter {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("FormalParameter", "");
            if(tokens.get(0).equals(new Token("void", Token.Type.KEYWORD))){
                Token token = tokens.get(0);
                tokens.remove(0);
                root.addChild(new ParseTreeNode("KEYWORD", "void"));
            } else if(Arrays.asList(ParameterList.getFirst()).contains(tokens.get(0))){
                root.addChild(ParameterList.process());
            }
            return root;
        }
    }

    /**
     * &lt;ParameterList&gt; ::= &lt;Parameter&gt; {, &lt;Parameter&gt;}
     */
    private static class ParameterList {
        public static Token[] getFirst() {
            return Parameter.getFirst();
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("ParameterList", "");
            root.addChild(Parameter.process());
            Token token = tokens.get(0);
            while(token.equals(new Token(",", Token.Type.DIVIDE_OP))){
                tokens.remove(0);
                root.addChild(Parameter.process());
                token = tokens.get(0);
            }
            return root;
        }
    }

    /**
     * &lt;Parameter&gt; ::= int &lt;Id&gt;
     */
    private static class Parameter {
        public static Token[] getFirst() {
            return new Token[]{new Token("int", Token.Type.KEYWORD)};
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Parameter", "");
            Token token = tokens.get(0);
            if(token.equals(new Token("int", Token.Type.KEYWORD))){
                tokens.remove(0);
                root.addChild(new ParseTreeNode("KEYWORD", token.getContent()));
                root.addChild(Id.process());
            } else {
                throw new ParseException(tokens.get(0).getContent());
            }
            return root;
        }
    }

    /**
     * &lt;StatementBlocks&gt; ::= '{' &lt;InnerDeclarative&gt; &lt;StatementChain&gt; '}'
     */
    private static class StatementBlocks {
        public static Token[] getFirst(){
            return new Token[]{new Token("{", Token.Type.LEFT_BRACE)};
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("StatementBlocks", "");
            if(!tokens.get(0).equals(new Token("{", Token.Type.LEFT_BRACE))){
                throw new ParseException(tokens.get(0).getContent());
            }
            root.addChild(new ParseTreeNode("LEFT_BRACE", tokens.get(0).getContent()));
            tokens.remove(0);
            root.addChild(InnerDeclarative.process());
            root.addChild(StatementChain.process());
            if(!tokens.get(0).equals(new Token("}", Token.Type.RIGHT_BRACE)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("RIGHT_BRACE", "}"));
            tokens.remove(0);
            return root;
        }
    }

    /**
     * &lt;InnerDeclarative ::= EMPTY | {&lt;InnerVariableDeclarative&gt; ;}
     */
    private static class InnerDeclarative {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("InnerDeclarative", "");
            while(Arrays.asList(InnerVariableDeclarative.getFirst()).contains(tokens.get(0))){
                root.addChild(InnerVariableDeclarative.process());
                if(!tokens.get(0).equals(new Token(";", Token.Type.FIELD_OP)))
                    throw new ParseException(tokens.get(0).getContent());
                tokens.remove(0);
                root.addChild(new ParseTreeNode("FIELD_OP", ";"));
            }
            return root;
        }
    }

    /**
     * &lt;InnerVariableDeclarative ::= int &lt;Id&gt;
     */
    private static class InnerVariableDeclarative {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("InnerVariableDeclaractive", "");
            if(!tokens.get(0).equals(new Token("int", Token.Type.KEYWORD)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("KEYWORD", tokens.get(0).getContent()));
            tokens.remove(0);
            root.addChild(Id.process());
            return root;
        }

        public static Token[] getFirst() {
            return new Token[]{new Token("int", Token.Type.KEYWORD)};
        }
    }

    /**
     * &lt;StatementChain&gt; ::= {&lt;Statement&gt;}
     */
    private static class StatementChain {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("StatementChain", "");
            root.addChild(Statement.process());
            while(Arrays.asList(Statement.getFirst()).contains(tokens.get(0))){
                root.addChild(Statement.process());
            }
            return root;
        }
    }

    /**
     * &lt;Statement&gt; ::= &lt;IfStatement&gt; | &lt;WhileStatement&gt; | &lt;ReturnStatement&gt; | &lt;AssignmentStatement&gt;
     */
    private static class Statement {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Statement", "");
            if(Arrays.asList(IfStatement.getFirst()).contains(tokens.get(0))){
                root.addChild(IfStatement.process());
            } else if(Arrays.asList(ReturnStatement.getFIrst()).contains(tokens.get(0))){
                root.addChild(ReturnStatement.process());
            } else if(Arrays.asList(WhileStatement.getFirst()).contains(tokens.get(0))){
                root.addChild(WhileStatement.process());
            } else if(Arrays.asList(AssignmentStatement.getFirst()).contains(tokens.get(0))){
                root.addChild(AssignmentStatement.process());
            } else throw new ParseException(tokens.get(0).getContent());
            return root;
        }

        public static Token[] getFirst() {
            Token[] ifFirst = IfStatement.getFirst();
            Token[] whileFirst = WhileStatement.getFirst();
            Token[] returnFirst = ReturnStatement.getFIrst();
            Token[] assignmentFirst = AssignmentStatement.getFirst();
            return new Token[]{ifFirst[0], whileFirst[0], returnFirst[0], assignmentFirst[0]};
        }
    }

    /**
     * &lt;IfStatement&gt; ::= if '(' &lt;Expression&gt; ')' &lt;StatementBlock&gt; [ else &lt;StatementBlock&gt; ]
     */
    private static class IfStatement {
        public static Token[] getFirst() {
            return new Token[]{new Token("if", Token.Type.KEYWORD)};
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("IfStatement", "");
            if(!tokens.get(0).equals(new Token("if", Token.Type.KEYWORD)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("KEYWORD", "if"));
            tokens.remove(0);
            if(!tokens.get(0).equals(new Token("(", Token.Type.LEFT_PARENTHESIS)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("LEFT_PARENTHESIS", "("));
            tokens.remove(0);
            root.addChild(Expression.process());
            if(!tokens.get(0).equals(new Token(")", Token.Type.RIGHT_PARENTHESIS)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("RIGHT_PARENTHESIS", ")"));
            tokens.remove(0);
            root.addChild(StatementBlocks.process());
            if(!tokens.get(0).equals(new Token("else", Token.Type.KEYWORD)))
                return root;
            root.addChild(new ParseTreeNode("KEYWORD", "else"));
            tokens.remove(0);
            root.addChild(StatementBlocks.process());
            return root;
        }
    }

    /**
     * &lt;ReturnStatement&gt; ::= return [ &lt;Expression&gt; ] ;
     */
    private static class ReturnStatement {
        public static Token[] getFIrst() {
            return new Token[]{new Token("return", Token.Type.KEYWORD)};
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("ReturnStatement", "");
            if(!tokens.get(0).equals(new Token("return", Token.Type.KEYWORD)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("KEYWORD", "return"));
            tokens.remove(0);
            if(Arrays.asList(Expression.getFirst()).contains(tokens.get(0))){
                root.addChild(Expression.process());
            }
            if(!tokens.get(0).equals(new Token(";", Token.Type.FIELD_OP)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("FIELD_OP", ";"));
            tokens.remove(0);
            return root;
        }
    }

    /**
     * &lt;WhileStatement&gt; ::= while '(' &lt;Expression&gt; ')' &lt;StatementBlock&gt;
     */
    private static class WhileStatement {
        public static Token[] getFirst() {
            return new Token[]{new Token("while", Token.Type.KEYWORD)};
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("WhileStatement", "");
            if(!tokens.get(0).equals(new Token("while", Token.Type.KEYWORD)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("KEYWORD", "while"));
            tokens.remove(0);
            if(!tokens.get(0).equals(new Token("(", Token.Type.LEFT_PARENTHESIS)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("LEFT_PARENTHESIS", "("));
            tokens.remove(0);
            root.addChild(Expression.process());
            if(!tokens.get(0).equals(new Token(")", Token.Type.RIGHT_PARENTHESIS)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("RIGHT_PARENTHESIS", ")"));
            tokens.remove(0);
            root.addChild(StatementBlocks.process());
            return root;
        }
    }

    /**
     * &lt;AssignmentStatement&gt; ::= &lt;Id&gt;=&lt;Expression&gt;
     */
    private static class AssignmentStatement {
        public static Token[] getFirst() {
            return Id.getFirst();
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("AssignmentStatement", "");
            root.addChild(Id.process());
            if(!tokens.get(0).equals(new Token("=", Token.Type.ASSIGNMENT)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("ASSIGNMENT", "="));
            tokens.remove(0);
            root.addChild(Expression.process());
            if(!tokens.get(0).equals(new Token(";", Token.Type.FIELD_OP)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("FIELD_OP", ";"));
            tokens.remove(0);
            return root;
        }
    }

    /**
     * &lt;Expression&gt; ::= &lt;PlusExpression&gt; {&lt;Relop&gt; &lt;PlusExpression&gt;}
     */
    private static class Expression {
        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Expression", "");
            root.addChild(PlusExpression.process());
            while(Arrays.asList(Relop.getFirst()).contains(tokens.get(0))){
                root.addChild(Relop.process());
                root.addChild(PlusExpression.process());
            }
            return root;
        }

        public static Token[] getFirst() {
            return PlusExpression.getFirst();
        }
    }

    /**
     * &lt;Relop&gt; ::= &lt; | &lt;= | &gt; | &gt;= | == | !=
     */
    private static class Relop {
        public static Token[] getFirst() {
            return new Token[]{
                    new Token("<", Token.Type.OPERATOR),
                    new Token("<=", Token.Type.OPERATOR),
                    new Token(">", Token.Type.OPERATOR),
                    new Token(">=", Token.Type.OPERATOR),
                    new Token("==", Token.Type.OPERATOR),
                    new Token("!=", Token.Type.OPERATOR)
            };
        }

        public static ParseTreeNode process() throws ParseException {
            Token token = tokens.get(0);
            if(token.getType() == Token.Type.OPERATOR){
                String content = token.getContent();
                if(content.equals("<") || content.equals("<=") || content.equals(">") || content.equals(">=")
                        || content.equals("==") || content.equals("!=")){
                    tokens.remove(0);
                    return new ParseTreeNode("RELOP", content);
                } else {
                    throw new ParseException(token.getContent());
                }
            } else {
                throw new ParseException(token.getContent());
            }
        }
    }

    /**
     * &lt;PlusExpression&gt; ::= &lt;Item&gt; {+ &lt;Item&gt; | - &lt;Item&gt;}
     */
    private static class PlusExpression {
        public static Token[] getFirst() {
            return Item.getFirst();
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("PlusExpression", "");
            root.addChild(Item.process());
            while (tokens.get(0).equals(new Token("+", Token.Type.OPERATOR))
                    || tokens.get(0).equals(new Token("-", Token.Type.OPERATOR))) {
                if(tokens.get(0).equals(new Token("+", Token.Type.OPERATOR))){
                    root.addChild(new ParseTreeNode("OPERATOR", "+"));
                } else {
                    root.addChild(new ParseTreeNode("OPERATOR", "-"));
                }
                tokens.remove(0);
                root.addChild(Item.process());
            }
            return root;
        }
    }

    /**
     * &lt;Item ::= &lt;Factor&gt; {*&lt;Factor&gt; | /&lt;Factor&gt;}
     */
    private static class Item {
        public static Token[] getFirst() {
            return Factor.getFirst();
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Item", "");
            root.addChild(Factor.process());
            while (tokens.get(0).equals(new Token("*", Token.Type.OPERATOR))
                    || tokens.get(0).equals(new Token("/", Token.Type.OPERATOR))) {
                if(tokens.get(0).equals(new Token("*", Token.Type.OPERATOR))){
                    root.addChild(new ParseTreeNode("OPERATOR", "*"));
                } else {
                    root.addChild(new ParseTreeNode("OPERATOR", "/"));
                }
                tokens.remove(0);
                root.addChild(Factor.process());
            }
            return root;
        }
    }

    /**
     * &lt;Factor ::= VALUE | '(' &lt;Expression&gt; ')' | &lt;Id&gt; &lt;Call&gt;
     */
    private static class Factor {
        public static Token[] getFirst() {
            return new Token[]{
                    new Token("", Token.Type.VALUE),
                    new Token("(", Token.Type.LEFT_PARENTHESIS),
                    Id.getFirst()[0]
            };
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Factor", "");
            if(tokens.get(0).equals(new Token("", Token.Type.VALUE))){
                root.addChild(new ParseTreeNode("VALUE", tokens.get(0).getContent()));
                tokens.remove(0);
            } else if(tokens.get(0).equals(new Token("(", Token.Type.LEFT_PARENTHESIS))){
                root.addChild(new ParseTreeNode("LEFT_PARENTHESIS", "("));
                tokens.remove(0);
                root.addChild(Expression.process());
                if(!tokens.get(0).equals(new Token(")", Token.Type.RIGHT_PARENTHESIS)))
                    throw new ParseException(tokens.get(0).getContent());
                root.addChild(new ParseTreeNode("RIGHT_PARENTHESIS", ")"));
                tokens.remove(0);
            } else if(tokens.get(0).equals(Id.getFirst()[0])){
                root.addChild(Id.process());
                if(Arrays.asList(Call.getFirst()).contains(tokens.get(0)))
                    root.addChild(Call.process());
            } else
                throw new ParseException(tokens.get(0).getContent());
            return root;
        }
    }

    /**
     * &lt;Call ::= '(' &lt;TrueParameterList&gt; ')'
     */
    private static class Call {
        public static Token[] getFirst() {
            return new Token[]{new Token("(", Token.Type.LEFT_PARENTHESIS)};
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("Call", "");
            if(!tokens.get(0).equals(new Token("(", Token.Type.LEFT_PARENTHESIS)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("LEFT_PARENTHESIS", "("));
            tokens.remove(0);
            if(Arrays.asList(TrueParameterList.getFirst()).contains(tokens.get(0))){
                root.addChild(TrueParameterList.process());
            }
            if(!tokens.get(0).equals(new Token(")", Token.Type.RIGHT_PARENTHESIS)))
                throw new ParseException(tokens.get(0).getContent());
            root.addChild(new ParseTreeNode("RIGHT_PARENTHESIS", ")"));
            tokens.remove(0);
            return root;
        }
    }

    /**
     * &lt;TrueParameterList&gt; ::= &lt;Expression&gt; {, &lt;Expression&gt;}
     */
    private static class TrueParameterList {
        public static Token[] getFirst(){
            return Expression.getFirst();
        }

        public static ParseTreeNode process() throws ParseException {
            ParseTreeNode root = new ParseTreeNode("TrueParameterList", "");
            root.addChild(Expression.process());
            while(tokens.get(0).equals(new Token(",", Token.Type.DIVIDE_OP))){
                root.addChild(new ParseTreeNode("DIVIDE_OP", ","));
                tokens.remove(0);
                root.addChild(Expression.process());
            }
            return root;
        }
    }
}
