package lexer;

import static control.Control.ConLexer.dump;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.LinkedList;
import lexer.Token.Kind;
import util.Todo;

public class Lexer
{
  public String fname; // the input file name to be compiled
  public InputStream fstream; // input stream for the above file
  public PushbackInputStream push; // pushBack input stream for the above file;
  public Token newToken;
  public int line;
  public int column;
  public int column_next;
  public LinkedList<Token> Tlist;
  
  public Lexer(String fname, InputStream fstream)
  {
    this.fname = fname;
    this.fstream = fstream;
    this.push = new PushbackInputStream(fstream);
    this.line = 1;
    this.column = 1;
    this.column_next = 1;
    this.Tlist = new LinkedList<Token>();
    this.newToken = null;
  }
  
  private void addLine(int c)
  {
	if ('\n' == c)
	{
		this.line++;
		this.Tlist.clear();
	}
  }
  
  private void subLine(int c)
  {
	if ('\n' == c)
	{
		this.line--;
	}
  }
  
  private void addColumn(int c)
  {
	if ('\n' == c)
	{
		this.column_next = 1;
	}
	else if ('\t' == c)
	{
		this.column_next += 4;
	}
	else
	{
		this.column_next++;
	}
  }
  
  // exercise7: Read the MiniJava specification and 
  // study carefully the forming rules for each kind of token
  private boolean isInteger(int c)
  {
	  if ('0' <= c && c <= '9')
	  {
		  return true;
	  }
	  else
	  {
		  return false;
	  }
  }
  
  private boolean isCharacter(int c)
  {
	  if (('a' <= c && c <= 'z') ||('A' <= c && c <= 'Z'))
	  {
		  return true;
	  }
	  else
	  {
		  return false;
	  }
  }
  
  // when reading a character '/', get ready to skip notes;
  private int skipNotes(int character) throws Exception
  {
	  int c = character;
	  c = this.push.read();
	  // in the part of notes, there is no need to count the number of columns;
	  if ('/' == c)
	  {
	    while ('\n' != (c = this.push.read()))
		{
		  ;
		}
	    this.addLine(c);
		this.addColumn(c);
	    c = this.push.read();
	    this.addColumn(c);
		return c;
	  }
	  else
	  {
		this.push.unread(c);
		this.column_next--;
		System.out.println("Bad input /Error !");
		return -1;
	  }
  }

  private boolean eatChar() throws Exception
  {
	  int c = 0;
	  c = this.push.read();
	  this.addLine(c);
	  this.addColumn(c);
	  while (c != ' ' && c != '\t' && c != '\n' && c != '.' && c != ';' 
			  && c != '+' && c != '-' && c != '*' && c != '&' && c != '='
			  && c != '>' && c != '<' && c != '!' && c != '(' && c != ')'
			  && c != '[' && c != ']' && c != '{' && c != '}')
	  {
		  c = this.push.read();
		  this.addLine(c);
		  this.addColumn(c);
	  }
	  if (' ' == c || '\t' == c || '\n' == c)
	  {
		  return true;
	  }
	  // Go back;
	  else if (c != '.' && c != ';' 
			  && c != '+' && c != '-' && c != '*' && c != '&' && c != '='
			  && c != '>' && c != '<' && c != '!' && c != '(' && c != ')'
			  && c != '[' && c != ']' && c != '{' && c != '}')
	  {
		  this.push.unread(c);
		  this.subLine(c);
		  this.column_next--;
		  return true;
	  }
	  else
	  {
		  System.out.println("Eat bad character!");
		  return false;
	  }
  }
  
  private Token generateToken(String s)
  {
	  String temp = s;
	  switch(s)
	  {
	  case "boolean":
		  this.newToken = new Token(Kind.TOKEN_BOOLEAN, this.getLine(), this.column, "boolean");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "class": 
		  this.newToken = new Token(Kind.TOKEN_CLASS, this.getLine(), this.column, "class");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "else":
		  this.newToken = new Token(Kind.TOKEN_ELSE,this.getLine(), this.column, "else");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "if":
		  this.newToken = new Token(Kind.TOKEN_IF, this.getLine(), this.column, "if");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "extends":
		  this.newToken = new Token(Kind.TOKEN_EXTENDS, this.getLine(), this.column, "extends");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "int":
		  this.newToken = new Token(Kind.TOKEN_INT, this.getLine(), this.column, "int");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "length":
		  this.newToken = new Token(Kind.TOKEN_LENGTH, this.getLine(), this.column, "length");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "main":
		  this.newToken = new Token(Kind.TOKEN_MAIN, this.getLine(), this.column, "main");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "new":
		  this.newToken = new Token(Kind.TOKEN_NEW, this.getLine(), this.column, "new");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "out":
		  this.newToken = new Token(Kind.TOKEN_OUT, this.getLine(), this.column, "out");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "println":
		  this.newToken = new Token(Kind.TOKEN_PRINTLN, this.getLine(), this.column, "println");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "public":
		  this.newToken = new Token(Kind.TOKEN_PUBLIC,this.getLine(), this.column, "public");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "return":
		  this.newToken = new Token(Kind.TOKEN_RETURN, this.getLine(), this.column, "return");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "static":
		  this.newToken = new Token(Kind.TOKEN_STATIC, this.getLine(), this.column, "static");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "String":
		  this.newToken = new Token(Kind.TOKEN_STRING, this.getLine(), this.column, "String");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "System":
		  this.newToken = new Token(Kind.TOKEN_SYSTEM, this.getLine(), this.column, "System");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "this":
		  this.newToken = new Token(Kind.TOKEN_THIS, this.getLine(), this.column, "this");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "true":
		  this.newToken = new Token(Kind.TOKEN_TRUE, this.getLine(), this.column, "true");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "false":
		  this.newToken = new Token(Kind.TOKEN_FALSE, this.getLine(), this.column, "false");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "void":
		  this.newToken = new Token(Kind.TOKEN_VOID, this.getLine(), this.column, "void");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  case "while":
		  this.newToken = new Token(Kind.TOKEN_WHILE, this.getLine(), this.column, "while");
		  this.Tlist.add(this.newToken);
		  return this.newToken;
	  default:
		  return null;
			  
	  }
  }
  
  private int getLine()
  {
	  return this.line;
  }
  
  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception	
  {
    // int c = this.fstream.read(); it will contain a bug when we read a '('
	// it do not contain the character pushed back;	  
    int c = this.push.read();
    int tag = 1;
    
    // this is the start position of the token;
    this.column = this.column_next;
    
    this.addLine(c);
    this.addColumn(c);
	if (-1 == c)
	{
    	// The value for "lineNum" is now "null",
    	// you should modify this to an appropriate
    	// line number for the "EOF" token.
		this.newToken = new Token(Kind.TOKEN_EOF, this.getLine(), this.column, "EOF");
		this.Tlist.add(newToken);
    	return this.newToken;
	}
	
    while (' ' == c || '\t' == c || '\n' == c)
    {
    	c = this.push.read();
		this.addLine(c);
		this.addColumn(c);
		// Notes can only be the beginning of the line.
	    while ('/' == c)
	    {
	      c = this.skipNotes(c);
	      if (c == '\n')
	      {
	    	this.addLine(c);
	    	this.addColumn(c);
	      }
	    }
    } 
   
//    c = this.push.read();
	
    if (-1 == c)
    {
    	this.newToken = new Token(Kind.TOKEN_EOF, this.getLine(), this.column, "EOF");
    	this.Tlist.add(newToken);
    	return this.newToken;
    }
    
    // having read a charactor;
    this.column = this.column_next - 1;
    switch (c) {
    case '+':
    	this.newToken = new Token(Kind.TOKEN_ADD, this.getLine(), this.column, "+");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '-':
    	this.newToken = new Token(Kind.TOKEN_SUB, this.getLine(), this.column, "-");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '*':
    	this.newToken = new Token(Kind.TOKEN_TIMES, this.getLine(), this.column, "*");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '(':
    	this.newToken = new Token(Kind.TOKEN_LPAREN, this.getLine(), this.column, "(");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case ')':
    	this.newToken = new Token(Kind.TOKEN_RPAREN, this.getLine(), this.column, ")");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '{':
    	this.newToken = new Token(Kind.TOKEN_LBRACE, this.getLine(), this.column, "{");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '}':
    	this.newToken = new Token(Kind.TOKEN_RBRACE, this.getLine(), this.column, "}");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '[':
    	this.newToken = new Token(Kind.TOKEN_LBRACK, this.getLine(), this.column, "[");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case ']':
    	this.newToken = new Token(Kind.TOKEN_RBRACK, this.getLine(), this.column, "]");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case ',':
    	this.newToken = new Token(Kind.TOKEN_COMMER, this.getLine(), this.column, ",");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '.':
    	this.newToken = new Token(Kind.TOKEN_DOT, this.getLine(), this.column, ".");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case ';':
    	this.newToken = new Token(Kind.TOKEN_SEMI, this.getLine(), this.column, ";");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '<':
    	this.newToken = new Token(Kind.TOKEN_LT, this.getLine(), this.column, "<");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '>':
    	this.newToken = new Token(Kind.TOKEN_GT, this.getLine(), this.column, ">");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '!':
    	this.newToken = new Token(Kind.TOKEN_NOT, this.getLine(), this.column, "!");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '=':
    	this.newToken = new Token(Kind.TOKEN_ASSIGN, this.getLine(), this.column, "=");
    	this.Tlist.add(newToken);
    	return this.newToken;
    case '&':
    	c = this.push.read();
    	this.addLine(c);
    	this.addColumn(c);
    	if (c == '&')
    	{
    		this.newToken = new Token(Kind.TOKEN_AND,this.getLine(), this.column, "&&");
    		this.Tlist.add(newToken);
    		return this.newToken;
    	}
    	else
    	{
    		this.push.unread(c);
    		this.subLine(c);
    		this.column_next--;
    		System.out.println("Error: Bad argument &...");
    		return null;
    	}
    default:
    	// Lab 1, exercise 2: supply missing code to
    	// lex other kinds of tokens.
    	// Hint: think carefully about the basic
    	// data structure and algorithms. The code
    	// is not that much and may be less than 50 lines. If you
    	// find you are writing a lot of code, you
    	// are on the wrong way.
    	String temp = "";

    	while (this.isInteger(c))
    	{
    		temp = temp + (char)c;
    		if ('0' == c && tag == 1)
    		{
    			tag = 0;
    			c = this.push.read();
    			this.addLine(c);
    			this.addColumn(c);
    			if (' ' == c || '\t' == c || '\n' == c)
    	        {
    				this.newToken = new Token(Kind.TOKEN_NUM, this.getLine(), this.column, "0");
    	        	this.Tlist.add(newToken);
    				return this.newToken;
    	        }
    			// print(0);
    			else if (c == '.' || c == ';' 
    					|| c == '+' || c == '-' || c == '*' || c == '&' || c == '='
    					|| c == '>' || c == '<' || c == '!' || c == ')')
            	{
    				this.push.unread(c);
    				this.subLine(c);
    				this.column_next--;
    				this.newToken = new Token(Kind.TOKEN_NUM, this.getLine(), this.column, "0");
            		this.Tlist.add(newToken);
    				return this.newToken;
            	}
    			else
    			{
    				System.out.println("Error: Bad number 0...");
    				this.push.unread(c);
    				this.subLine(c);
    				return null;
    			}
    		}
    		
    		// begin with 1~9
    		c = this.push.read();
    		this.subLine(c);
    		this.addColumn(c);
    		tag = 0;
        }
        if (' ' == c || '\t' == c || '\n' == c)
        {
        	this.newToken = new Token(Kind.TOKEN_NUM, this.getLine(), this.column, temp);
        	this.Tlist.add(newToken);
        	return this.newToken;
        }
        else if (c == '.' || c == ';' 
  			  || c == '+' || c == '-' || c == '*' || c == '&' || c == '='
  			  || c == '>' || c == '<' || c == '!' || c == '(' || c == ')'
  			  || c == '[' || c == ']' || c == '{' || c == '}')
        {
        	this.push.unread(c);
        	this.subLine(c);
        	this.column_next--;
        	this.newToken = new Token(Kind.TOKEN_NUM, this.getLine(), this.column, temp);
        	this.Tlist.add(newToken);
        	return this.newToken;
        }
        else if (tag == 0)
        {
        	System.out.println("Error: Bad argument 1...");
        	return null;
        }    
        else
        {
        	while (this.isCharacter(c))
        	{  	
        		temp = "" + (char)c;
        		Token t = null;
        		c = this.push.read();
        		this.addLine(c);
        		this.addColumn(c);
        		while (this.isInteger(c)|| this.isCharacter(c)|| c == '_')
        		{
        			temp = temp + (char)c;
        			c = this.push.read();
        			this.addLine(c);
        			this.addColumn(c);
        		}
        		if (' ' == c || '\t' == c || '\n' == c)
        		{
        			t = this.generateToken(temp);
        			if (null == t)
        			{
        				this.newToken = new Token(Kind.TOKEN_ID, this.getLine(), this.column, temp);
        				this.Tlist.add(newToken);
        				return this.newToken;
        			}
        			else
        			{
        				return t;
        			}
        		}
        		else if (c == '.' || c == ';' 
        				|| c == '+' || c == '-' || c == '*' || c == '&' || c == '='
        				||c == '>' || c == '<' || c == '!' || c == '(' || c == ')'
        				|| c == '[' || c == ']' || c == '{' || c == '}' || c == ',')
        		{		
        			this.push.unread(c);
        			this.subLine(c);
        			this.column_next--;
        			t = this.generateToken(temp);
        			if (null == t)
        			{
        				this.newToken = new Token(Kind.TOKEN_ID, this.getLine(), this.column, temp);
        				this.Tlist.add(newToken);
        				return this.newToken;
        			}
        			else
        			{
        				return t;
        			}
        		}
        		else
        		{
        			System.out.println("Error: Bad argument A...");
        			return null;
        		}
        	}
        	return null;
        }
    }
  }

  public Token nextToken()
  {
    Token t = null;

    try {
      t = this.nextTokenInternal();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (dump)
      System.out.println(t.toString());
    return t;
  }
}
