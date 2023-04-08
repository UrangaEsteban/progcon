%%writefile ProcessStuff.java
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.StringReader;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.util.*;

public class ProcessStuff {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("You need to pass an argument");
            return;
        }
        String self = String.valueOf(args[0]);
        Gson gson = new Gson();
        Type customType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
        HashMap<String, ArrayList<String>> dataMap = gson.fromJson(getFileData(DATA_FILE_NAME), customType); 
        
        info(self);

        ArrayList<Process> children = spawnChildren(dataMap, self);
        for(Process child : children) {
            try {
              child.waitFor();
            } catch (InterruptedException e) {
              System.err.println("Can spawn no more, we've been interrupted.");
            }
        }
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          System.err.println("Can spawn no more, we've been interrupted.");
        }
    }

    public static String getFileData(String fileName) {
        String data = "";
        try {
            data = String.join("", Files.readAllLines(FileSystems.getDefault().getPath(fileName)));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Couldn't read the file " + fileName);
            e.printStackTrace();
        }
        return data;
    }

    public synchronized static void info(String self) {
        ProcessHandle selfProcessHandle = ProcessHandle.current();
        try (
            PrintWriter out = new PrintWriter(new FileOutputStream(OUT, true), true);
        ) {
            out.print("I am: " + self + ". ");
            out.print("My PID is: " + selfProcessHandle.pid() + ". ");
            out.print("My PPID is: " + selfProcessHandle.parent().get().pid() + ".\n");
            out.flush();
        } catch (IOException e) {
            System.err.println("Couldn't open file: " + OUT);
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            System.err.println("So sorry, I've no parent: " + OUT);
            e.printStackTrace();
        }
    }

    public static ArrayList<Process> spawnChildren(HashMap<String, ArrayList<String>> dataMap, String self) {
        ArrayList<Process> children = new ArrayList<>();
        if (dataMap.keySet().contains(self)) {
            for(String childValue : dataMap.get(self)) {
              try {
                children.add(new ProcessBuilder("java", "-cp", ".:gson-2.10.1.jar:gson-2.10.1.jar:", "ProcessStuff", childValue, "1>salidaJava", "2>errors").start());
              } catch (IOException e) {
                System.err.println("Couldn't spawn new process:  " + childValue);
              }
            }
        }
        return children;
    }

    final static String DATA_FILE_NAME = "data.json";
    final static String OUT = "miSalidaJava";
}