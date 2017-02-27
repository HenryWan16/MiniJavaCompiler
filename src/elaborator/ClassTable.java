package elaborator;

import ast.Ast.Type;
import util.Todo;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Collection;

public class ClassTable
{
  // map each class name (a string), to the class bindings.
  private java.util.Hashtable<String, ClassBinding> table;

  public ClassTable()
  {
    this.table = new java.util.Hashtable<String, ClassBinding>();
  }

  // Duplication is not allowed
  public void put(String c, ClassBinding cb)
  {
    if (this.table.get(c) != null) {
      System.out.println("duplicated class: " + c);
      System.exit(1);
    }
    this.table.put(c, cb);
  }

  // put a field into this table
  // Duplication is not allowed
  public void put(String c, String id, Type.T type)
  {
    ClassBinding cb = this.table.get(c);
    cb.put(id, type);
    return;
  }

  // put a method into this table
  // Duplication is not allowed.
  // Also note that MiniJava does NOT allow overloading.
  public void put(String c, String id, MethodType type)
  {
    ClassBinding cb = this.table.get(c);
    cb.put(id, type);
    return;
  }

  // return null for non-existing class
  public ClassBinding get(String className)
  {
    return this.table.get(className);
  }

  // get type of some field
  // return null for non-existing field.
  public Type.T get(String className, String xid)
  {
    ClassBinding cb = this.table.get(className);
    Type.T type = cb.fields.get(xid);
    while (type == null) { // search all parent classes until found or fail
      if (cb.extendss == null)
        return type;

      cb = this.table.get(cb.extendss);
      type = cb.fields.get(xid);
    }
    return type;
  }

  // get type of some method
  // return null for non-existing method
  public MethodType getm(String className, String mid)
  {
    ClassBinding cb = this.table.get(className);
    MethodType type = cb.methods.get(mid);
    while (type == null) { // search all parent classes until found or fail
      if (cb.extendss == null)
        return type;

      cb = this.table.get(cb.extendss);
      type = cb.methods.get(mid);
    }
    return type;
  }

  public void dump()
  {
	String name = null, xid = null, methodName = null;
	Type.T type = null;
	MethodType typem = null;
	ClassBinding cb = null;

	Enumeration<String> e = null;
	e = this.table.keys();  
    for (Enumeration<String> en = e; en.hasMoreElements();)
    {
      // ClassSingle name;
      name = en.nextElement();
      cb = this.table.get(name);
      System.out.println("--------------------------------------------------");
      System.out.printf("%-20s", "Class");
      System.out.printf("%-20s", name);
      System.out.printf("%-20s", "Field:");
      System.out.println("");
      System.out.printf("%-20s", "Name");
      System.out.printf("%-20s", "Type");
      System.out.println("");
      for (Enumeration<String> temp = cb.fields.keys(); temp.hasMoreElements();)
      {
    	xid = temp.nextElement();
    	type = cb.fields.get(xid);   	
        System.out.printf("%-20s", xid);
        System.out.printf("%-20s", type);
        System.out.println("");
      }
      System.out.println("--------------------------------------------------");
      System.out.println("");
      
      System.out.println("--------------------------------------------------");
  	  System.out.printf("%-20s", "Class");
      System.out.printf("%-20s", name);
      System.out.printf("%-20s", "Method:");
      System.out.println("");
      System.out.printf("%-20s", "Name");
      System.out.printf("%-20s", "RetType");
      System.out.println("");
      for (Enumeration<String> temp = cb.methods.keys(); temp.hasMoreElements();)
      {
    	methodName = temp.nextElement();
    	typem = cb.methods.get(methodName);   	       
        System.out.printf("%-20s", methodName);
        System.out.printf("%-20s", typem.retType);
        System.out.println("");      
      } 
      System.out.println("--------------------------------------------------");
      System.out.println("");
    }
  }

  @Override
  public String toString()
  {
    return this.table.toString();
  }
}
