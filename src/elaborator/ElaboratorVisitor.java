package elaborator;

import java.util.LinkedList;

import parser.Parser;
import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.False;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Length;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewIntArray;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Not;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.True;
import ast.Ast.MainClass;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Type.ClassType;
import control.Control.ConAst;

public class ElaboratorVisitor implements ast.Visitor
{
  public ClassTable classTable; // symbol table for class
  public MethodTable methodTable; // symbol table for each method
  public String currentClass; // the class name being elaborated
  public Type.T type; // type of the expression being elaborated
  public String fname; // the file being elaborated
  public Parser parser;
  
  public ElaboratorVisitor()
  {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
    this.fname = null;
    this.parser = null;
  }
  
  public ElaboratorVisitor(String fname, Parser parser)
  {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
    this.fname = fname;
    this.parser = parser;
  }

  public void error()
  {
	System.out.print(this.fname + ":" + this.parser.current.lineNum + ":" + this.parser.current.column);
  }
  
  public void warn()
  {
	System.out.println("");
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
	e.left.accept(this);
	Type.T leftty = this.type;
	e.right.accept(this);
	if (!this.type.toString().equals(leftty.toString()))
	{
      error();
      System.out.println("	error:" + this.type.toString() + "Can not be cast to " + leftty.toString());
	}
	this.type = new Type.Int();
	return;
  }

  @Override
  public void visit(And e)
  {
	e.left.accept(this);
	Type.T leftty = this.type;
	e.right.accept(this);
	if (!this.type.toString().equals(leftty.toString()))
	{
	  error();
	  System.out.println("	error:" + this.type.toString() + "Can not be cast to " + leftty.toString());
	}
	this.type = new Type.Int();
	return;
  }

  @Override
  public void visit(ArraySelect e)
  {
	Type.T arrayName = null;
	e.array.accept(this);
	arrayName = this.methodTable.get(e.array.toString());
	if (arrayName == null)
	{
	  arrayName = this.classTable.get(this.currentClass, e.array.toString());
	}
	// arrayName is not in ClassTable or in MethodTable
	if (arrayName == null)
	{
	  error();
	  System.out.println("	error:" + arrayName + "has not been defined! " + arrayName.toString());
	  return;
	}
	// arrayName is not int []
	if (!(this.type.toString().equals("@int[]")))
	{
	  error();
	  System.out.println("	error:" + this.type.toString() + "Can not be cast to int[]");
	  return;
	}
	// index is not int
	e.index.accept(this);
	if (!(this.type.toString().equals("@int")))
	{
	  error();
	  System.out.println("	error:" + "The index " + "can not be cast to int!");
	  return;
	}
	// a[1] is int
	this.type = new Type.Int();
	return;
  }

  @Override
  public void visit(Call e)
  {
    Type.T leftty;
    Type.ClassType ty = null;

    e.exp.accept(this);
    leftty = this.type;
    if (leftty instanceof ClassType) {
      ty = (ClassType) leftty;
      e.type = ty.id;
    } else {
      error();
      System.out.println("	error:" + this.type.toString() + "Can not be cast to " + "ClassType");
    }
    MethodType mty = this.classTable.getm(ty.id, e.id);

    java.util.LinkedList<Type.T> declaredArgTypes
    = new java.util.LinkedList<Type.T>();
    for (Dec.T dec: mty.argsType){
      declaredArgTypes.add(((Dec.DecSingle)dec).type);
    }
    java.util.LinkedList<Type.T> argsty = new LinkedList<Type.T>();
    for (Exp.T a : e.args) {
      a.accept(this);
      argsty.addLast(this.type);
    }
    if (declaredArgTypes.size() != argsty.size())
      error();
    // For now, the following code only checks that
    // the types for actual and formal arguments should
    // be the same. However, in MiniJava, the actual type
    // of the parameter can also be a subtype (sub-class) of the 
    // formal type. That is, one can pass an object of type "A"
    // to a method expecting a type "B", whenever type "A" is
    // a sub-class of type "B".
    // Modify the following code accordingly:
    for (int i = 0; i < argsty.size(); i++) {
    //=======
    //    if (mty.argsType.size() != argsty.size())
    //    {
    //      error();
    //      System.out.println("	error:Method parameters Can not apply to given type");
    //    }
    //    for (int i = 0; i < argsty.size(); i++) {
    //>>>>>>> Lab2
      Dec.DecSingle dec = (Dec.DecSingle) mty.argsType.get(i);
      if (dec.type.toString().equals(argsty.get(i).toString()))
        ;
      else {
        error();
        System.out.println("	error:" + dec.type.toString() + "Can not be cast to given type");
      }
    }
    this.type = mty.retType;
    // the following two types should be the declared types.
    e.at = declaredArgTypes;
    e.rt = this.type;
    return;
  }

  @Override
  public void visit(False e)
  {
	this.type = new Type.Boolean();
	return;
  }

  @Override
  public void visit(Id e)
  {
    // first look up the id in method table
    Type.T type = this.methodTable.get(e.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass, e.id);
      // mark this id as a field id, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null) {
      error();
      System.out.println("	error:" + e.id + "has not been defined! ");
    }
    this.type = type;
    // record this type on this node for future use.
    e.type = type;
    return;
  }

  @Override
  public void visit(Length e)
  {
	Type.T type = null;
	e.array.accept(this);
	type = this.methodTable.get(e.array.toString());
	if (type == null)
	{
	  type = this.classTable.get(this.currentClass, e.array.toString());
	}
	if (type == null)
	{
	  error();
	  System.out.println("	error:" + e.array.toString() + "has not been defined! ");
	  return;
	}
	if (!(this.type.toString().equals("@int[]")))
	{
	  error();
	  System.out.println("	error:" + this.type.toString() + "can not be cast to int[]!");
	  return;
	}
	this.type = new Type.Int();
	return;
  }

  @Override
  public void visit(Lt e)
  {
    e.left.accept(this);
    Type.T ty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(ty.toString())) {
      error();
      System.out.println("	error:" + this.type.toString() + "can not be cast to " + ty.toString());
    }
    this.type = new Type.Boolean();
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
	e.exp.accept(this);
	if (!this.type.toString().equals("@int"))
	{
	  error();
	  System.out.println("	error:" + this.type.toString() + "can not be cast to int!");
	}
	return;
  }

  @Override
  public void visit(NewObject e)
  {
    this.type = new Type.ClassType(e.id);
    return;
  }

  @Override
  public void visit(Not e)
  {
	e.exp.accept(this);
	if (this.type instanceof Type.T)
	{
	  this.type = new Type.Boolean();
	}
	else
	{
	  error();
	  System.out.println("	error:" + this.type.toString() + "can not be cast to all the Type!");
	}
	return;
  }

  @Override
  public void visit(Num e)
  {
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(Sub e)
  {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString())) {
      error();
      System.out.println("	error:" + this.type.toString() + "Can not be cast to " + leftty.toString());
    }
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(This e)
  {
    this.type = new Type.ClassType(this.currentClass);
    return;
  }

  @Override
  public void visit(Times e)
  {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString())) {
      error();
      System.out.println("	error:" + this.type.toString() + "Can not be cast to " + leftty.toString());
    }
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(True e)
  {
	this.type = new Type.Boolean();
	return;
  }

  // statements
  @Override
  public void visit(Assign s)
  {
    // first look up the id in method table
    Type.T type = this.methodTable.get(s.id);
    // if search failed, then s.id must
    if (type == null)
      type = this.classTable.get(this.currentClass, s.id);
    if (type == null) {
      error();
      System.out.println("	error:" + s.id + "has not been defined! ");
    }
    s.exp.accept(this);
    s.type = type;
    this.type.toString().equals(type.toString());
    return;
  }

  @Override
  public void visit(AssignArray s)
  {
	Type.T type1 = null, type2 = null;
	Type.T type = this.methodTable.get(s.id);
	if (type == null)
	{
	  type = this.classTable.get(this.currentClass, s.id);
	}
	if (type == null)
	{
	  error();
	  System.out.println("	error:" + s.id + "has not been defined! ");
	}
	else if (!type.toString().equals("@int[]"))
	{
	  error();
	  System.out.println("	error:" + this.type.toString() + "can not be cast to int[]!");
	}
	else
	{
	  s.exp.accept(this);
	  type1 = this.type;
	  s.index.accept(this);
	  type2 = this.type;
	  if (!(type1.toString().equals("@int")))
	  {
		error();
		System.out.println("	error:" + type1.toString() + "can not be cast to int!");
	  }
	  else if (!(type2.toString().equals("@int")))
	  {
		error();
		System.out.println("	error:" + this.type.toString() + "can not be cast to int!");
	  }
	  this.type = new Type.Int();
	}
  }
  

  @Override
  public void visit(Block s)
  {
	for (Stm.T st : s.stms)
	{
	  st.accept(this);
	}
	return;
  }

  @Override
  public void visit(If s)
  {
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean"))
    {
      error();
      System.out.println("	error:" + this.type.toString() + "can not be cast to boalean!");
    }
    s.thenn.accept(this);
    s.elsee.accept(this);
    return;
  }

  @Override
  public void visit(Print s)
  {
    s.exp.accept(this);
    if (!this.type.toString().equals("@int"))
    {
      error();
      System.out.println("	error:" + s.exp.toString() + "can not be cast to int!");
    }
    return;
  }

  @Override
  public void visit(While s)
  {
	Type.T type = null;
	s.condition.accept(this);
	s.body.accept(this);
	return;
  }

  // type
  @Override
  public void visit(Type.Boolean t)
  {
	System.out.println("Boolean");
	return;
  }

  @Override
  public void visit(Type.ClassType t)
  {
	System.out.println("Class");
	return;
  }

  @Override
  public void visit(Type.Int t)
  {
    System.out.println("int");
    return;
  }

  @Override
  public void visit(Type.IntArray t)
  {
	System.out.println("int[]");
  }

  // dec
  @Override
  public void visit(Dec.DecSingle d)
  {
	// field ?
	Type.T type = null;
	this.classTable.put(this.currentClass, d.id, d.type);
	return;
  }

  // method
  @Override
  public void visit(Method.MethodSingle m)
  {
	// init the method table to put the same name in different method
	this.methodTable = new MethodTable();  
	
    // construct the method table
    this.methodTable.put(m.formals, m.locals);

    if (ConAst.elabMethodTable)
      this.methodTable.dump();

    for (Stm.T s : m.stms)
      s.accept(this);
    m.retExp.accept(this);
    return;
  }

  // class
  @Override
  public void visit(Class.ClassSingle c)
  {
    this.currentClass = c.id;

    for (Method.T m : c.methods) {
      m.accept(this);
    }
    return;
  }

  // main class
  @Override
  public void visit(MainClass.MainClassSingle c)
  {
    this.currentClass = c.id;
    // "main" has an argument "arg" of type "String[]", but
    // one has no chance to use it. So it's safe to skip it...

    c.stm.accept(this);
    return;
  }

  // ////////////////////////////////////////////////////////
  // step 1: build class table
  // class table for Main class
  private void buildMainClass(MainClass.MainClassSingle main)
  {
    this.classTable.put(main.id, new ClassBinding(null));
  }

  // class table for normal classes
  private void buildClass(ClassSingle c)
  {
	this.classTable = new ClassTable();
    this.classTable.put(c.id, new ClassBinding(c.extendss));
    for (Dec.T dec : c.decs) {
      Dec.DecSingle d = (Dec.DecSingle) dec;
      this.classTable.put(c.id, d.id, d.type);
    }
    for (Method.T method : c.methods) {
      MethodSingle m = (MethodSingle) method;
      this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals));
    }
  }

  // step 1: end
  // ///////////////////////////////////////////////////

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // ////////////////////////////////////////////////
    // step 1: build a symbol table for class (the class table)
    // a class table is a mapping from class names to class bindings
    // classTable: className -> ClassBinding{extends, fields, methods}
    buildMainClass((MainClass.MainClassSingle) p.mainClass);
    for (Class.T c : p.classes) {
      buildClass((ClassSingle) c);
    }

    // we can double check that the class table is OK!
    if (control.Control.ConAst.elabClassTable) {
      this.classTable.dump();
    }

    // ////////////////////////////////////////////////
    // step 2: elaborate each class in turn, under the class table
    // built above.
    p.mainClass.accept(this);
    for (Class.T c : p.classes) {
      c.accept(this);
    }

  }
}
