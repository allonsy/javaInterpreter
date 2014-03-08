import jline.console.ConsoleReader;
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
public class JSH
{
    ClassPathManager _classPathManager;
    InteractionsPaneOptions _interpreterOptions;
    Interpreter _interpreter;
    boolean fileFlag;
    public JSH()
    {
        _classPathManager=new ClassPathManager(ReflectUtil.SYSTEM_CLASS_PATH);
        _interpreterOptions=new InteractionsPaneOptions();
        _interpreter=new Interpreter(_interpreterOptions, _classPathManager.makeClassLoader(null));
        fileFlag=false;
    }
	public void _interpret(String toEval) {
	try {
      Option<Object> result = this._interpreter.interpret(toEval);
      if (result.isSome()) {
        String objString = null;
        try { 
                objString = TextUtil.toString(result.unwrap());
                if(!fileFlag)
                {
                    System.out.println(objString);
                }
            }
        catch (Throwable t) { throw new EvaluatorException(t); }
      }
    }
    catch (InterpreterException e) {
      System.out.println("Error");
    }
  }
  public void interMethod(String in, ConsoleReader console) throws IOException
  {
      console.setPrompt("");
      int braceCount=0;
      boolean quote=false;
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
          }
          else if(in.charAt(i)=='}' && !quote)
          {
              braceCount--;
          }
      }
      if(braceCount==0)
      {
        _interpret(in);
        console.setPrompt(">");
      }
      else
      {
          in=in+"\n"+console.readLine();
          interMethod(in, console);
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
  public static void main(String[] args) throws IOException
  {
      JSH inter=new JSH();
      ConsoleReader console=new ConsoleReader();
      console.setPrompt("> ");
      System.out.println("Welcome to the command line java interpreter!");
      System.out.println("Please call System.exit(0), or CTRL-D to quit");
      if(args.length>0)
      {
          inter.parseFile(args);
      }
      String input;
      input=console.readLine();
      while(input!=null)
      {
        if(input.contains("{"))
        {
            inter.interMethod(input, console);
            input=console.readLine();
        }
        else
        {
            inter._interpret(input);
            input=console.readLine();
        }
      }
      System.exit(0);
  }
}
