// This is automatically generated by the Tiger compiler.
// Do NOT modify!

// structures
struct Sum
{
  struct Sum_vtable *vptr;
};
struct Doit
{
  struct Doit_vtable *vptr;
};

// vtables structures
struct Sum_vtable
{
  char *Sum_gc_map;
};

struct Doit_vtable
{
  char *Doit_gc_map;
  int (*doit)();
};


// pointing to f's caller's GC frame
void *prev;

// methods
struct doit_gc_frame 
{
  void *prev;
  int arguments_gc_map;
  int *arguments_base_address;
  int local_gc_map;
  int sum;
  int i;
};

int Doit_doit(struct Doit * this, int n)
{
  int sum;
  int i;

  int f_arguments_gc_map = 1;
  int f_locals_gc_map = 0;
  struct doit_gc_frame frame;
  frame.prev = prev;
  prev = &frame;
  frame.arguments_gc_map = 1;
  frame.arguments_base_address = &this;
  frame.local_gc_map = 0;
  i = 0;  sum = 0;  while (i < n)
  {
        sum = sum + i;        i = i + 1;  }  return sum;
}

// vtables
struct Sum_vtable Sum_vtable_ = 
{
  "", 
};

struct Doit_vtable Doit_vtable_ = 
{
  "", 
  Doit_doit
};


// main method
int Tiger_main ()
{
  struct Doit * x_1;
  System_out_println ((x_1=((struct Doit*)(Tiger_new (&Doit_vtable_, sizeof(struct Doit)))), x_1->vptr->doit(x_1, 101)));
}




