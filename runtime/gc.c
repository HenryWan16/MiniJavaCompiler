#include <malloc.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

// The Gimple Garbage Collector.


//===============================================================//
// The Java Heap data structure.

/*   
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap
{
  int size;         // in bytes, note that this if for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

int begin = 0;
int end = 0;
int count = 0;
int printlog = 0;

static void Tiger_gc ();
void Tiger_heap_init (int heapSize);
void Tiger_heap_exchange();
void *Obj_node_init(void *vtable, int size);

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize)
{
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"
  char *space = (char *)malloc(heapSize);

  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.
  heap.size = heapSize/2;

  // #3: initialize the "from" field (with what value?)
  heap.from = space;

  // #4: initialize the "fromFree" field (with what value?)
  heap.fromFree = heap.from;

  // #5: initialize the "to" field (with what value?)
  heap.to = heap.from + heapSize/2;

  // #6: initizlize the "toStart" field with NULL;
  heap.toStart = heap.to;

  // #7: initialize the "toNext" field with NULL;
  heap.toNext = heap.toStart;

  return;
}

void Tiger_heap_exchange()
{
  char *temp;
  temp = heap.from;
  heap.from = heap.to;
  heap.to = temp;
  
  temp = heap.toNext;
  heap.toNext = heap.fromFree;
  heap.fromFree = temp;

  heap.toStart = heap.to;
  heap.toNext = heap.to;
  return;
}

void Tiger_print_log()
{
  printf("%d round of GC: ", count);
  printf("begin at %d , ", begin);
  printf("break up at %d , ", end);
  return;
}

// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
void *prev = NULL;

//===============================================================//
struct Node_O 
{
  void *vptr;
  int isObjOrArray;
  int length;
  char *forwarding;
  char **v;
};
typedef struct Node_O Obj_node;

void *Forward_O(Obj_node *q);
void *Chase_O(Obj_node *st);

void *Obj_node_init(void *vtable, int size)
{
  int i = 0;
  Obj_node *p = (Obj_node *)malloc(sizeof(Obj_node));
  p->vptr = vtable;
  p->isObjOrArray = 0;
  p->length = 0;
  p->forwarding = NULL;
  p->v = (char *)malloc(sizeof(char) * size * 4);
  for (i = 0; i < size; i++)
  {
  	*(p->v + i) = NULL;
  }
  return p;
}

void *Forward_O(Obj_node *q)
{
  Obj_node *p = q;
  struct JavaHeap *h = &heap;
  // p points to from-space
  if ((void *)p - (void *)h->from >= 0 && (void *)p <= (void *)h->from + h->size)
  {
	// already has been copied
  	if ((void *)p->forwarding - (void *)h->to >= 0 && (void *)p->forwarding <= (void *)h->to + h->size)
	{
	  return p->forwarding;
	}
	// has not been copied
	else
	{
	  Chase_O(p);
	  return p->forwarding;
	}
  }
  // p points to to-space
  else
  {
    return p;
  }
}

// we need to get size from vptr
void *Chase_O(Obj_node *st)
{
  Obj_node *p = st;
  struct JavaHeap *h = &heap;
  // move the records
  Obj_node *q = NULL, *temp = NULL;
  void *r = NULL;
  int i = 0;
  while (p != NULL)
  {
  	q = (Obj_node *)h->toNext;
	h->toNext = h->toNext + sizeof(Obj_node);
    	r = NULL;
	q->vptr = p->vptr;
	q->isObjOrArray = p->isObjOrArray;
	q->length = p->length;
	q->forwarding = p->forwarding;
	q->v = p->v;
	temp = (Obj_node *)q->forwarding;
	if ((q->forwarding - h->from >= 0 && q->forwarding <= h->from + h->size) && 
      		!(temp->forwarding - h->to >= 0 && temp->forwarding <= h->to + h->size))
	{
	  r = q->forwarding; 
	}
	// indicating the current node has already been copied
	p->forwarding = q;
	p = r;
  }
  return p;
}

//===============================================================//
// Object Model And allocation

// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      | vptr      ---|----> (points to the virtual method table)
      |--------------|
      | isObjOrArray | (0: for normal objects)
      |--------------|
      | length       | (this field should be empty for normal objects)
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| v_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)


void *Tiger_new (void *vtable, int size)
{
  	// Your code here:
  	Obj_node *p = Obj_node_init(vtable, size);
  	// Tiger_heap_init(Control_heapSize);      
  	if (heap.from + heap.size - heap.fromFree >= sizeof(Obj_node))
  	{
	  	heap.fromFree= p;
	  	heap.fromFree += sizeof(Obj_node);
	  	p->forwarding = heap.fromFree;
    	}
  	else
  	{
		begin = clock()/CLOCKS_PER_SEC; 
  	  	Tiger_gc();
	  	Tiger_heap_exchange();
	  	end = clock()/CLOCKS_PER_SEC;
		count++;
		// printf log
		if (printlog == 1)
		{
			Tiger_print_log();
		}
	  	// after the collection, there is still no enough space
	  	if (heap.from + heap.size - heap.fromFree < sizeof(Obj_node))
	  	{
	    	System_out_println("OutOfMemory");
			return NULL;
	  	}
    	}
  	return p;
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      | vptr         | (this field should be empty for an array)
      |--------------|
      | isObjOrArray | (1: for array)
      |--------------|
      | length       |
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| e_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)

//===============================================================//
struct Node_A 
{
  void *vptr;
  int isObjOrArray;
  int length;
  void *forwarding;
  void *e;
};
typedef struct Node_A Array_node;

void *Chase_A(Array_node *st);

void *Array_node_init(int length)
{
  Array_node *p = (Array_node *)malloc(sizeof(Array_node));
  p->vptr = NULL;
  p->isObjOrArray = 1;
  p->length = length;
  p->forwarding = NULL;
  p->e = (int *)malloc(sizeof(length));
  return p;
}

void *Forward_A(Array_node *st)
{
  Array_node *p = st;
  struct JavaHeap *h = &heap;
  // p points to from-space
  if ((char *)p - h->from >= 0 && (char *)p <= h->from + h->size)
  {
	// already has been copied
  	if ((char *)p->forwarding - h->to >= 0 && (char *)p->forwarding <= h->to + h->size)
	{
	  return p->forwarding;
	}
	// has not been copied
	else
	{
	  Chase_A(p);
	  return p->forwarding;
	}
  }
  // p points to to-space
  else
  {
    return p;
  }
}

// we need to get size from vptr
void *Chase_A(Array_node *st)
{
  Array_node *p = st;
  struct JavaHeap *h = &heap;
  // move the records
  Array_node *q = NULL, *temp = NULL;
  Array_node *r = NULL;
  int i = 0;
  while (p != NULL)
  {
  	q = (Array_node *)h->toNext;
	h->toNext = h->toNext + sizeof(Obj_node);
    	r = NULL;
	q->vptr = p->vptr;
	q->isObjOrArray = p->isObjOrArray;
    	q->length = p->length;
	q->forwarding = p->forwarding;
	q->e = p->e;
	temp = (Array_node *)q->forwarding;
	if (((char *)q->forwarding - h->from >= 0 && (char *)q->forwarding <= h->from + h->size) && 
  	   !((char *)temp->forwarding - h->to >= 0 && (char *)temp->forwarding <= h->to + h->size))
	{
	  r = q->forwarding; 
	}
	// indicating the current node has already been copied
	p->forwarding = q;
	p = r;
  }
  return p;
}

void *Tiger_new_array (int length)
{
  	// Your code here:
  	Array_node *p = Array_node_init(length);
  	// Tiger_heap_init(Control_heapSize);
  	if (heap.from + heap.size - heap.fromFree >= sizeof(Array_node))
  	{
	  	heap.fromFree = p;
	  	heap.fromFree += sizeof(Array_node);
	  	p->forwarding = heap.fromFree;
    	}
  	else
  	{
		begin = clock()/CLOCKS_PER_SEC;
  	  	Tiger_gc();
	  	Tiger_heap_exchange();
		end = clock()/CLOCKS_PER_SEC;
		count++;
		// printf log
		if (printlog == 1)
		{
			Tiger_print_log();
		}
	  	// after the collection, there is still no enough space
	  	if (heap.from + heap.size - heap.fromFree < sizeof(Array_node))
	  	{
	        System_out_println("OutOfMemory");
			return NULL;
	  	}
    	}
  	return p;
}

//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
static void Tiger_gc ()
{
  // Your code here:
  // Copy the From space to To space!
  char *temp = heap.from;
  Obj_node *temp_O = (Obj_node *)temp;
  Array_node *temp_A = (Array_node *)temp;
  while (temp != (heap.from + heap.size))
  {
  	if (temp_O->isObjOrArray == 0)
  	{
  	  Obj_node *p = temp_O;
  	  temp = (char *)Forward_O(p);
    	}
  	else
  	{
      	  Array_node *p = temp_A;
	  temp = (char *)Forward_A(p);
    	}
  }

  // To space: make the forwarding pointer to point the right space;
  char *scan = heap.toStart;
  while (scan != heap.toNext)
  {
    Obj_node *scan_O = (Obj_node *)scan;
    Array_node *scan_A = (Array_node *)scan;
    if (scan_O->isObjOrArray == 0)
	{
	  scan_O->forwarding = scan_O + sizeof(Obj_node);
	  scan = scan_O->forwarding;
	}
	else
	{
	  scan_A->forwarding = scan_A + sizeof(Array_node);
	  scan = scan_A->forwarding;
	}
  }
  return;
}
