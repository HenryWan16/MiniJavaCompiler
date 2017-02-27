package parser;

import java.util.LinkedList;
import ast.Ast.Exp;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.True;
import ast.Ast.Exp.False;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.NewIntArray;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Length;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.Not;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.Lt;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.If;
import ast.Ast.Stm.While;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.Print;
import ast.Ast.Type;
import ast.Ast.Type.Int;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.IntArray;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser
{
  public Lexer lexer;
  public Token current;

  public Parser(String fname, java.io.InputStream fstream)
  {
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.

  private void advance()
  {
    current = lexer.nextToken();
  }

  private void eatToken(Kind kind)
  {
    if (kind == current.kind)
      advance();
    else {
      System.out.println("Expects: " + kind.toString());
      System.out.println("But got: " + current.kind.toString());
      System.exit(1);
    }
  }
  
  private void error()
  {
    System.out.println("Syntax error: compilation aborting...\n");
    System.exit(1);
    return;
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  private LinkedList<Exp.T> parseExpList()
  {
	Exp.T exp;
	LinkedList<Exp.T> elist = new LinkedList<Exp.T>();
    if (current.kind == Kind.TOKEN_RPAREN)
      return elist;
    exp = parseExp();
    elist.addLast(exp);
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      exp = parseExp();
      elist.addLast(exp);
    }
    return elist;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private Exp.T parseAtomExp()
  {
	Exp.T exp = null;
	
    switch (current.kind) {
    case TOKEN_RPAREN:
      return exp;
    case TOKEN_LPAREN:
      advance();
      exp = parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      return exp;
    case TOKEN_NUM:
      exp = new Num(Integer.parseInt(current.lexeme));
      advance();
      return exp;
    case TOKEN_TRUE:
      exp = new True();
      advance();
      return exp;
    case TOKEN_FALSE:
      exp = new False();
      advance();
      return exp;
    case TOKEN_THIS:
      exp = new This();
      advance();
      return exp;
    case TOKEN_ID:
      exp = new Id(current.lexeme);
      advance();
      return exp;
    case TOKEN_NEW: {
      advance();
      switch (current.kind) {
      case TOKEN_INT:
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        exp = new NewIntArray(parseExp());
        eatToken(Kind.TOKEN_RBRACK);
        return exp;
      case TOKEN_ID:
    	exp = new NewObject(current.lexeme);
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        return exp;
      default:
        error();
        return null;
      }
    }
    default:
      error();
      return null;
    }
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private Exp.T parseNotExp()
  {
	Exp.T exp = null;
	Exp.T temp;
	LinkedList<Exp.T> elist;
	String name = null;
    
	exp = parseAtomExp();
    if (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          exp = new Length(exp);
          advance();
          return exp;
        }
        name = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        elist = parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
        exp = new Call(exp, name, elist, current.lineNum, current.column);
      } else {
        advance();
        temp = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        exp = new ArraySelect(exp, temp, current.lineNum, current.column);
      }
    }
    return exp;
  }

  // TimesExp -> ! TimesExp
  // -> NotExp
  private Exp.T parseTimesExp()
  {
	Exp.T exp = null;
	
    if (current.kind == Kind.TOKEN_NOT) {
      advance();
      exp = new Not(parseTimesExp());
    }
    exp = parseNotExp();
    return exp;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private Exp.T parseAddSubExp()
  {
	Exp.T exp = null;
	
    exp = parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      exp = new Times(exp, parseTimesExp());
    }
    return exp;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private Exp.T parseLtExp()
  {
	Exp.T exp = null;
	
    exp = parseAddSubExp();
    if (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
      if (current.kind == Kind.TOKEN_ADD)
      {
    	advance();
        exp = new Add(exp, parseAddSubExp(), current.lineNum, current.column);
      }
      else
      {
    	advance();
        exp = new Sub(exp, parseAddSubExp());
      }
    }
    return exp;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private Exp.T parseAndExp()
  {
	Exp.T exp = null;
    exp = parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      exp = new Lt(exp, parseLtExp());
    }
    return exp;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private Exp.T parseExp()
  {
	Exp.T exp = null;
    exp = parseAndExp();
    if (current.kind == Kind.TOKEN_AND) {
      advance();
      exp = new And(exp, parseAndExp(), current.lineNum, current.column);
    }
    return exp;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;
  private Stm.T parseStatement()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a statement.
	Stm.T stm = null;
	Stm.T stm1 = null, stm2 = null;
	LinkedList<Stm.T> slist = null;
	Exp.T exp = null;
	Exp.T exp1 = null, exp2 = null;
	String name = null;
	
    switch (current.kind)
    {
    case TOKEN_RBRACE:
    	advance();
    	return stm;
    case TOKEN_LBRACE:
    	advance();
    	stm = parseStatement();
    	return stm;
    case TOKEN_IF:
    	advance();
    	eatToken(Kind.TOKEN_LPAREN);
    	exp = parseExp();
    	eatToken(Kind.TOKEN_RPAREN);
    	stm1 = parseStatement();
    	eatToken(Kind.TOKEN_ELSE);
    	stm2 = parseStatement();
    	stm = new If(exp, stm1, stm2);
    	return stm;
    case TOKEN_WHILE:
    	advance();
    	eatToken(Kind.TOKEN_LPAREN);
    	exp = parseExp();
    	eatToken(Kind.TOKEN_RPAREN);
    	eatToken(Kind.TOKEN_LBRACE);
    	slist = parseStatements();
    	eatToken(Kind.TOKEN_RBRACE);
    	stm = new While(exp, new Block(slist));
    	return stm;
    case TOKEN_SYSTEM:
    	advance();
    	eatToken(Kind.TOKEN_DOT);
    	eatToken(Kind.TOKEN_OUT);
    	eatToken(Kind.TOKEN_DOT);
    	eatToken(Kind.TOKEN_PRINTLN);
    	eatToken(Kind.TOKEN_LPAREN);	
    	exp = parseExp();
    	eatToken(Kind.TOKEN_RPAREN);
    	eatToken(Kind.TOKEN_SEMI);
    	stm = new Print(exp);
    	return stm;
    case TOKEN_ID:
    	name = current.lexeme;
    	advance();
    	switch(current.kind)
    	{
    	case TOKEN_LBRACK:
    		advance();
    		exp1 = parseExp();
    		eatToken(Kind.TOKEN_RBRACK);
    		eatToken(Kind.TOKEN_ASSIGN);
    		exp2 = parseExp();
    		eatToken(Kind.TOKEN_SEMI);
    		stm = new AssignArray(name, exp1, exp2);
    		return stm;
    	case TOKEN_ASSIGN:
    		advance();
    		exp1 = parseExp();
    		eatToken(Kind.TOKEN_SEMI);
    		stm = new Assign(name, exp1);
    		return stm;
    	default:
    		return stm;
    	}
    default:
    	return stm;
    }
    
  }

  // Statements -> Statement Statements
  // ->
  private LinkedList<Stm.T> parseStatements()
  {
	Stm.T stm;
	LinkedList<Stm.T> stms = new LinkedList<Stm.T>();
	
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE 
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
      stm = parseStatement();
      stms.addLast(stm);
    }
    return stms;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private Type.T parseType()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a type.
	Type.T type = null;

    switch(current.kind)
    {
    case TOKEN_INT:
    	advance();
    	
    	if (current.kind == Kind.TOKEN_LBRACK)
    	{
    		advance();
    		eatToken(Kind.TOKEN_RBRACK);
    		type = new IntArray(lexer.line, lexer.column);
    		return type;
    	}
    	else
    	{
    		type = new Int(lexer.line, lexer.column);
    		return type;
    	}
    case TOKEN_BOOLEAN:
    	type = new Boolean(lexer.line, lexer.column);
    	advance();
    	return type;
    case TOKEN_ID:
    	type = new ClassType(current.lexeme, lexer.line, lexer.column);
    	advance();
    	return type;
    default:
    	return null;
    }
  }

  // VarDecl -> Type id ;
  private Dec.T parseVarDecl()
  {
	Dec.DecSingle dec = null;
	Type.T type = null;
	String name = null;
	
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    type = parseType();
    name = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_SEMI);
    dec = new Dec.DecSingle(type, name, current.lineNum, current.column);
    return dec;
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private LinkedList<Dec.T> parseVarDecls()
  {
	Dec.T dec = null;
	LinkedList<Dec.T> dlist = new LinkedList<Dec.T>();
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      // int i; i = 10; Seperat these parts fix the bug and avoid to go back.
      if (current.kind == Kind.TOKEN_ID)
      {
    	  return dlist;
      }
      else 
      {
    	  dec = parseVarDecl();
    	  dlist.addLast(dec);
      }
    }
    return dlist;
  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id
  private LinkedList<Dec.T> parseFormalList()
  {
	Type.T type = null;
	Dec.DecSingle dec = null;
	LinkedList<Dec.T> dlist = new LinkedList<Dec.T>();
	
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      type = parseType();
      dec = new Dec.DecSingle(type, current.lexeme, current.lineNum, current.column);
      dlist.addLast(dec);
      eatToken(Kind.TOKEN_ID);
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        type = parseType();
        dec = new Dec.DecSingle(type, current.lexeme, current.lineNum, current.column);
        eatToken(Kind.TOKEN_ID);      
        dlist.addLast(dec);
      }
    }
    return dlist;
  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  private Method.MethodSingle parseMethod()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a method.
	Type.T type = null;
	Exp.T exp = null;
	LinkedList<Dec.T> dlist1 = new LinkedList<Dec.T>();
	LinkedList<Dec.T> dlist2 = new LinkedList<Dec.T>();
	LinkedList<Stm.T> stms = new LinkedList<Stm.T>();		
	Method.MethodSingle method = null;
	String name = null;
	
    eatToken(Kind.TOKEN_PUBLIC);
    type = parseType();
    name = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LPAREN);
    dlist1 = parseFormalList();
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    dlist2 = parseVarDecls();
    
    stms = parseStatements();
    eatToken(Kind.TOKEN_RETURN);
    exp = parseExp();
    eatToken(Kind.TOKEN_SEMI);
    eatToken(Kind.TOKEN_RBRACE);
    method = new Method.MethodSingle(type, name, dlist1, dlist2, stms, exp);
    return method;
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private LinkedList<Method.T> parseMethodDecls()
  {
	Method.MethodSingle method = null;
	LinkedList<Method.T> mlist = new LinkedList<Method.T>();
	
    while (current.kind == Kind.TOKEN_PUBLIC) {
      method = parseMethod();
      mlist.addLast(method);
    }
    return mlist;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private Class.ClassSingle parseClassDecl()
  {
	LinkedList<Dec.T> dlist = new LinkedList<Dec.T>();
	LinkedList<Method.T> mlist = new LinkedList<Method.T>();
	Class.ClassSingle clas = null;
	String name1 = null, name2 = null;
	
    eatToken(Kind.TOKEN_CLASS);
    name1 = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      name2 = current.lexeme;
      eatToken(Kind.TOKEN_ID);
      eatToken(Kind.TOKEN_LBRACE);
      dlist = parseVarDecls();
      mlist = parseMethodDecls();
      eatToken(Kind.TOKEN_RBRACE);
      clas = new Class.ClassSingle(name1, name2, dlist, mlist);
      return clas;
    }
    eatToken(Kind.TOKEN_LBRACE);
    dlist = parseVarDecls();
    mlist = parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    clas = new Class.ClassSingle(name1, null, dlist, mlist);
    return clas;
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private LinkedList<Class.T> parseClassDecls()
  {
	LinkedList<Class.T> clist = new LinkedList<Class.T>();
	Class.ClassSingle clas = null;
	
    while (current.kind == Kind.TOKEN_CLASS) {
      clas = parseClassDecl();
      clist.addLast(clas);
    }
    return clist;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statement
  // }
  // }
  private MainClass.MainClassSingle parseMainClass()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a main class as described by the
    // grammar above.
	Stm.T stm = null;
	MainClass.MainClassSingle mainClass = null;
	String name1 = null;
	String name2 = null;
	
	eatToken(Kind.TOKEN_CLASS);
	name1 = current.lexeme;
	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_LBRACE);
	eatToken(Kind.TOKEN_PUBLIC);
	eatToken(Kind.TOKEN_STATIC);
	eatToken(Kind.TOKEN_VOID);
	eatToken(Kind.TOKEN_MAIN);
	eatToken(Kind.TOKEN_LPAREN);
	eatToken(Kind.TOKEN_STRING);
	eatToken(Kind.TOKEN_LBRACK);
	eatToken(Kind.TOKEN_RBRACK);
	name2 = current.lexeme;
	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_RPAREN);
	eatToken(Kind.TOKEN_LBRACE);
//	advance();
	stm = parseStatement();
	eatToken(Kind.TOKEN_RBRACE);
	eatToken(Kind.TOKEN_RBRACE);
	mainClass = new MainClass.MainClassSingle(name1, name2, stm);
	return mainClass;
  }

  // Program -> MainClass ClassDecl*
  private Program.ProgramSingle parseProgram()
  {
	MainClass.MainClassSingle mainClass = null;
	Program.ProgramSingle program = null;
	LinkedList<Class.T> clist = new LinkedList<Class.T>();
	
    mainClass = parseMainClass();
    clist = parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    program = new Program.ProgramSingle(mainClass, clist);
    return program;
  }

  public ast.Ast.Program.T parse()
  {
	Program.ProgramSingle program = null;
    program = parseProgram();
    return program;
  }
}
