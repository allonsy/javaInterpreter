import edu.rice.cs.drjava.model.repl.newjvm.ClassPathManager;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.Interpreter;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.drjava.model.repl.InteractionsPaneOptions;
import java.io.IOException;
import edu.rice.cs.drjava.model.repl.InteractionsModel;
public class JSH
{
    ClassPathManager _classPathManager;
    InteractionsPaneOptions _interpreterOptions;
    Interpreter _interpreter;
    boolean fileFlag;
    public long pointer;
    public String prompt;
    public byte single;
    
     static //JNI
    {
        System.loadLibrary("Readline");
    }
    public native String readLine(String prompt, int flag);
    
    
    public JSH()
    {
        _classPathManager=new ClassPathManager(ReflectUtil.SYSTEM_CLASS_PATH);
        _interpreterOptions=new InteractionsPaneOptions();
        _interpreter=new Interpreter(_interpreterOptions, _classPathManager.makeClassLoader(null));
        fileFlag=false;
        pointer=0;
        prompt=">>> ";
        single=1;
    }
	public void _interpret(String toEval) 
    {
	try 
    {
      Option<Object> result = this._interpreter.interpret(toEval);
      if (result.isSome()) 
      {
        String objString = null;
        try 
        { 
                objString = TextUtil.toString(result.unwrap());
                if(!fileFlag)
                {
                    System.out.println(objString);
                }
            }
        catch (Throwable t) { throw new EvaluatorException(t); }
      }
    }
    catch(koala.dynamicjava.parser.wrapper.ParseError e)
    {
        System.out.println("HI");
    }
    catch (InterpreterException e) 
    {
      
      if(e.getMessage().equals("koala.dynamicjava.parser.wrapper.ParseError: Encountered Unexpected \"<EOF>\""))
      {
          try
          {
            interMethod(toEval);
          }
          catch(IOException ex)
          {
              System.out.println(e.getMessage());
          }
      }
      else
      {
          System.out.println(e.getMessage());
      }
    }
  }
  private boolean containsOneLine(String s)
  {
	  int count=0;
	  for(int i=0; i<s.length(); i++)
	  {
		  if(s.charAt(i)=='\n')
		  count++;
	  }
	  if(count==1)
		  return true;
	  return false;
  }
  public void interMethod(String in) throws IOException
  {
      prompt="... ";
      int braceCount=0;
      boolean quote=false, flag=false;
      for(int i=0; i<in.length(); i++)
      {
          if(in.charAt(i)=='"' && i==0)
          {
              quote=!quote;
          }
          else if((in.charAt(i)=='"')&&(!(in.charAt(i-1)=='\\'))&&(!(in.charAt(i-1)=='\'')))
          {
              quote=!quote;
          }
          else if(in.charAt(i)=='{' && !quote)
          {
              braceCount++;
              flag=true;
          }
          else if(in.charAt(i)=='}' && !quote)
          {
              braceCount--;
          }
      }
      if(braceCount==0&&flag)
      {
        _interpret(in);
        prompt="> ";
      }
	  else if((!(flag)) && containsOneLine(in))
	  {
		  //System.out.println("enter the fist");
		  _interpret(in);
		  this.prompt="> ";
	  }
      else
      {
          in=in+"\n"+this.readLine(prompt, single);
          interMethod(in);
      }
  }
  public void parseFile(String [] args)
  {
      fileFlag=true;
      for(int i=0; i<args.length; i++)
      {
          EasyReader reader=new EasyReader(args[i]);
          String f="";
          f=reader.readLine();
          while(!reader.eof())
          {
              f=f+"\n"+reader.readLine();
          }
          _interpret(f);
          System.out.println("Succesfully imported file: "+args[i]);
      }
      fileFlag=false;
  }
  public void _notifyInteractionIncomplete() {
    System.out.println("Hello World");
  }
  public static void main(String[] args) throws IOException
  {
      JSH inter=new JSH();
      inter.prompt="> ";
      System.out.println("Welcome to the command line java interpreter!");
      System.out.println("Please call System.exit(0), or CTRL-D to quit");
      if(args.length>0)
      {
          inter.parseFile(args);
      }
      String input;
      input=inter.readLine(inter.prompt, inter.single);
      inter.single=0;
      while(input!=null)
      {
        if(input.contains("{"))
        {
            inter.interMethod(input);
            input=inter.readLine(inter.prompt, inter.single);
        }
        else
        {
            inter._interpret(input);
            input=inter.readLine(inter.prompt, inter.single);
        }
      }
      System.exit(0);
  }
}
