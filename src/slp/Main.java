package slp;

import java.io.FileWriter;
import java.util.HashSet;

import slp.Slp.Exp;
import slp.Slp.Exp.Eseq;
import slp.Slp.Exp.Id;
import slp.Slp.Exp.Num;
import slp.Slp.Exp.OP_T;
import slp.Slp.Exp.Op;
import slp.Slp.ExpList;
import slp.Slp.ExpList.Pair;
import slp.Slp.ExpList.T;
import slp.Slp.Stm;
import util.Bug;
import util.Todo;
import control.Control;

public class Main
{
	private Table table = new Table("Default",-2,null);
	private IntAndTable int_table;
	
	// /////////////////////////////////////////////////////////////////////////
	// exercise4: Table
	private class Table
	{
		private String id;
		private int value;
		private Table tail;
		
		// mapping identifiers to the integer values assigned to them
		public Table(String i, int v, Table t)
		{
			this.id = i;
			this.value = v;
			this.tail = t;
		}
		public Table(Table t)
		{
			this.id = t.id;
			this.value = t.value;
			this.tail = t.tail;
		}
		public Table()
		{
			this.id = "Default";
			this.value = -2;
			this.tail = null;
		}
		
		// putting a new cell at the head of the linked list
		public void update(Table t, String i, int v)
		{
			Table t1 = new Table(t);
			this.id = i;
			this.value = v;
			this.tail = t1;
			// making a new cell t2 = update(t1, i, v);
		}
		
		// searches down the linked list and the first 
		// occurrence of c in the list takes precedence over any later occurrence
		public int lookup(String key)
		{
			Table temp = this;
			while(temp.id != key && temp.id != "Default")
			{
				temp = temp.tail;
			}
			if (temp.id == key)
			{
				return temp.value;
			}
			else
			{
				return -1;
			}
		}
	}
	
	// It is always modified by interpExp() and different interpExp() will 
	// get different IntAndTable
	private class IntAndTable
	{
		private int i;
		private Table t;
		
		public IntAndTable(int ii, Table tt)
		{
			this.i = ii;
			this.t = tt;
		}		
	}
	
	// exercise3: tells the maximum number of arguments of any print statement
	private int maxArgs(Stm.T stm)
	{
		int numArgsPrint = 0;
		Stm.Print temp = (Stm.Print) stm;
		while (!(temp.explist instanceof ExpList.Last))
		{
			if (temp.explist instanceof ExpList.Pair)
			{
				numArgsPrint++;
				temp.explist = ((ExpList.Pair)temp.explist).list;
			}
			else 
			{
				new Bug();
			    return -1;
			}
		}
		numArgsPrint++;
		return numArgsPrint;
	}
	
  // ///////////////////////////////////////////
  // maximum number of args

  private int maxArgsExp(Exp.T exp)
  {
	  if (exp instanceof Exp.Eseq) 
	  {
	      Exp.Eseq e = (Exp.Eseq)exp;
	      int n1 = maxArgsStm(e.stm);
	      int n2 = maxArgsExp(e.exp);
	      return n1 >= n2 ? n1 : n2;
	  }
	  else if (exp instanceof Exp.Id)
	  {
		  return 0;
	  }
	  else if (exp instanceof Exp.Num)
	  {
		  return 0;
	  }
	  else if (exp instanceof Exp.Op)
	  {
		  return 0;
	  }
	  else
	  {
		  new Bug();
		  return 0;
	  }
  }

  private int maxArgsStm(Stm.T stm)
  {
    if (stm instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) stm;
      int n1 = maxArgsStm(s.s1);
      int n2 = maxArgsStm(s.s2);

      return n1 >= n2 ? n1 : n2;
    } else if (stm instanceof Stm.Assign) {
      int num = maxArgsExp(((Stm.Assign) stm).exp);
      return num;
    } else if (stm instanceof Stm.Print) {
      int num = maxArgs(stm);
      return num;
    } else
      new Bug();
    return 0;
  }
  
  //  "interprets" a program in this language SLP. 
  //  To write in a "functional programming" style 
  //  - in which you never update old states, but generate new ones.
  private void interp(Stm.T s)
  {
	  Stm.Assign temp = (Stm.Assign)s;
	  Table t = new Table(this.table);
	  int num = 0;
	  
	  // Security?
	  // need to insert a cell
	  this.interpExp(temp.exp);
	  num = this.int_table.i;
	  this.table.update(t, temp.id, num);
	  return;
  }
  
  // ////////////////////////////////////////
  // interpreter

  private void interpExp(Exp.T exp)
  {
	  Table temp = new Table();
	  if (exp instanceof Exp.Id)
	  {
		  Exp.Id e = (Exp.Id) exp;
		  
		  // When you count Exp op Exp, it will be a bug on the right argument
		  temp.id = e.id;
		  temp.value = this.table.lookup(temp.id);
		  this.int_table = new IntAndTable(temp.value,temp);
	  }
	  else if (exp instanceof Exp.Num)
	  {
		  Exp.Num e = (Exp.Num) exp;
		  
		  temp.value = e.num;
		  temp.id = "NoId";
		  this.int_table = new IntAndTable(e.num,temp);
	  }
	  else if (exp instanceof Exp.Op)
	  {
		  Exp.Op o = (Exp.Op) exp;
		  interpExp(o.left);
		  IntAndTable i_t = this.int_table;
		  interpExp(o.right);
		  
		  if (o.op == OP_T.ADD)
		  {
			  //int_table will soon be modified
			  temp.value = i_t.i + this.int_table.i;
			  this.int_table = new IntAndTable(temp.value,temp);
		  }
		  else if (o.op == OP_T.SUB)
		  {
			  temp.value = i_t.i - this.int_table.i;
			  this.int_table = new IntAndTable(temp.value,temp);
		  }
		  else if (o.op == OP_T.TIMES)
		  {
			  temp.value = i_t.i * this.int_table.i;
			  this.int_table = new IntAndTable(temp.value,temp);
		  }
		  else if (o.op == OP_T.DIVIDE)
		  {
			  // need to decide divide 0
			  if (this.int_table.i == 0)
			  {
				  System.out.println("Error: Divided 0");
				  temp.id = "Divided0";
				  this.int_table = new IntAndTable(-1,temp);
			  }
			  else
			  {
				  this.table.value = i_t.i / this.int_table.i;
				  this.int_table = new IntAndTable(temp.value,temp);
			  }
		  }
		  else
		  {
			  new Bug();
		  }
	  }
	  else if (exp instanceof Exp.Eseq)
	  {
		  interpStm(((Exp.Eseq) exp).stm);
		  interpExp(((Exp.Eseq) exp).exp);
		  return;
	  }
	  else
	  {
		  new Bug();
	  }
  }

  private void interpStm(Stm.T prog)
  {
    if (prog instanceof Stm.Compound) 
    {
    	Stm.Compound s = (Stm.Compound) prog;
        interpStm(s.s1);
        interpStm(s.s2);
    } else if (prog instanceof Stm.Assign) {
    	interp(prog);
    } else if (prog instanceof Stm.Print) {
    	Stm.Print p = (Stm.Print) prog;
    	T temp = p.explist;
    	
    	while (temp instanceof ExpList.Pair)
    	{
    		ExpList.Pair temp_pair = (ExpList.Pair) temp;
    		interpExp(temp_pair.exp);
    		System.out.print(this.int_table.i + " ");
    		temp = temp_pair.list;
    	}
    	if (temp instanceof ExpList.Last)
    	{
    		interpExp(((ExpList.Last) temp).exp);
    		System.out.println(this.int_table.i);
    	}
    } else
      new Bug();
    return;
  }

  // ////////////////////////////////////////
  // compile
  HashSet<String> ids;
  StringBuffer buf;
  boolean Zero;
  
  private void emit(String s)
  {
    buf.append(s);
  }

  private void compileExp(Exp.T exp)
  {
    if (exp instanceof Id) {
      Exp.Id e = (Exp.Id) exp;
      String id = e.id;

      emit("\tmovl\t" + id + ", %eax\n");
    } else if (exp instanceof Num) {
      Exp.Num e = (Exp.Num) exp;
      int num = e.num;
      
      // exercise5: It is only a tag which will be used in the future to decide 1/0
      if (num == 0)
      {
    	  Zero = true;
      }
      emit("\tmovl\t$" + num + ", %eax\n");
    } else if (exp instanceof Op) {
      Exp.Op e = (Exp.Op) exp;
      Exp.T left = e.left;
      Exp.T right = e.right;
      Exp.OP_T op = e.op;

      switch (op) {
      case ADD:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\taddl\t%edx, %eax\n");
        break;
      case SUB:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\tsubl\t%eax, %edx\n");
        emit("\tmovl\t%edx, %eax\n");
        break;
      case TIMES:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\timul\t%edx\n");
        break;
      case DIVIDE:
        compileExp(left);
        emit("\tpushl\t%eax\n");       
        compileExp(right);          
        // exercise5:  exit more gracefully for "divide by zero"
        if (Zero == true)
        {
        	System.out.println("divide by zero");
        	break;
        }       
        emit("\tpopl\t%edx\n");
        emit("\tmovl\t%eax, %ecx\n");
        emit("\tmovl\t%edx, %eax\n");
        emit("\tcltd\n");
        emit("\tdiv\t%ecx\n");
        break;
      default:
        new Bug();
      }
    } else if (exp instanceof Eseq) {
      Eseq e = (Eseq) exp;
      Stm.T stm = e.stm;
      Exp.T ee = e.exp;

      compileStm(stm);
      compileExp(ee);
    } else
      new Bug();
  }

  private void compileExpList(ExpList.T explist)
  {
    if (explist instanceof ExpList.Pair) {
      ExpList.Pair pair = (ExpList.Pair) explist;
      Exp.T exp = pair.exp;
      ExpList.T list = pair.list;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
      compileExpList(list);
    } else if (explist instanceof ExpList.Last) {
      ExpList.Last last = (ExpList.Last) explist;
      Exp.T exp = last.exp;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  private void compileStm(Stm.T prog)
  {
    if (prog instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) prog;
      Stm.T s1 = s.s1;
      Stm.T s2 = s.s2;

      compileStm(s1);
      compileStm(s2);
    } else if (prog instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) prog;
      String id = s.id;
      Exp.T exp = s.exp;

      ids.add(id);
      compileExp(exp);
      emit("\tmovl\t%eax, " + id + "\n");
    } else if (prog instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) prog;
      ExpList.T explist = s.explist;

      compileExpList(explist);
      emit("\tpushl\t$newline\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  // ////////////////////////////////////////
  public void doit(Stm.T prog)
  {
    // return the maximum number of arguments
    if (Control.ConSlp.action == Control.ConSlp.T.ARGS) {
      int numArgs = maxArgsStm(prog);
      System.out.println(numArgs);
    }
  
    // interpret a given program
    if (Control.ConSlp.action == Control.ConSlp.T.INTERP) {
      interpStm(prog);
    }

    // compile a given SLP program to x86
    if (Control.ConSlp.action == Control.ConSlp.T.COMPILE) {
      ids = new HashSet<String>();
      buf = new StringBuffer();

      compileStm(prog);
      try {
        // FileOutputStream out = new FileOutputStream();
        FileWriter writer = new FileWriter("slp_gen.s");
        writer
            .write("// Automatically generated by the Tiger compiler, do NOT edit.\n\n");
        writer.write("\t.data\n");
        writer.write("slp_format:\n");
        writer.write("\t.string \"%d \"\n");
        writer.write("newline:\n");
        writer.write("\t.string \"\\n\"\n");
        for (String s : this.ids) {
          writer.write(s + ":\n");
          writer.write("\t.int 0\n");
        }
        writer.write("\n\n\t.text\n");
        writer.write("\t.globl main\n");
        writer.write("main:\n");
        writer.write("\tpushl\t%ebp\n");
        writer.write("\tmovl\t%esp, %ebp\n");
        writer.write(buf.toString());
        writer.write("\tleave\n\tret\n\n");
        writer.close();
        Process child = Runtime.getRuntime().exec("gcc slp_gen.s");
        child.waitFor();
        
        if (!Control.ConSlp.keepasm)
          Runtime.getRuntime().exec("rm -rf slp_gen.s");
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
      // System.out.println(buf.toString());
    }
  }
}
