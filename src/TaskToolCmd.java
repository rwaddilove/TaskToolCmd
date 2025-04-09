// Task Tool - a text-based command line task/to-do manager
// By Roland Waddilove (github.com/rwaddilove/) as an exercise
// while learning Java. Public Domain. Use at your own risk!

// Get rid of numbers in Edit - use field names

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

class Input {
    public static String InputStr(String prompt, int len) {
        System.out.print(prompt);
        Scanner input = new Scanner(System.in);
        String inp = input.nextLine().strip();
        return (inp.length() > len) ? inp.substring(0, len) : inp; }

    public static int InputInt(String prompt) {
        try {
            return Integer.parseInt(InputStr(prompt, 6)); }
        catch (NumberFormatException e) {
            return 9999; } }     // a value not used

    public static char InputChr(String prompt) {
        String inp = InputStr(prompt, 3).toLowerCase();
        return inp.isBlank() ? '*' : inp.charAt(0); }

    public static boolean isNumber(String s) {
        for (char c : s.toCharArray())
            if (!Character.isDigit(c)) return false;
        return true; }

    public static String InputDate(String prompt) {
        String inp = InputStr(prompt,15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        try {
            LocalDate date = LocalDate.parse(inp, formatter);}
        catch (DateTimeParseException dtpe) {
            System.out.println("Date not set (not recognised).");
            inp = ""; }
        return inp;
    }
}


class FileOp {

    public static void Read(String fp, ArrayList<ArrayList<String>> tasks) {
        tasks.clear();
        try (Scanner readFile = new Scanner(new File(fp))) {
            while (readFile.hasNextLine()) {
                String line = readFile.nextLine();
                line = line.substring(1, line.length()-1);        // strip first and last "
                String[] values = line.split("\",\"");      // split into task fields
                tasks.add(new ArrayList<>());
                for (String value : values)
                    tasks.getLast().add(value);
            }
        }
        catch (FileNotFoundException e) { System.out.println("'" + fp + "' not found."); }
    }

    public static void Write(String fp, ArrayList<ArrayList<String>> tasks) {
        try (FileWriter writeFile = new FileWriter(fp)) {
            for (ArrayList<String> tsk : tasks ) {
                String line = "\"";
                for (String s : tsk)
                    line += s + "\",\"";
                writeFile.write(line.substring(0, (line.length()-2)) + "\n");
            }
        }
        catch (IOException e) { System.out.println("Could not save " + fp); }
    }
}


class Task {

    public static void Show(ArrayList<ArrayList<String>> tasks) {
        int i = 0;
        System.out.println("\n   Title                Due        Repeat  Label      Done");
        for (ArrayList<String> tsk : tasks ) {
            String title = tsk.getFirst();
            if (title.length() > 18) title = title.substring(0,16) + "..";
            System.out.printf("%2d %-20s %-10s %-7s %-10s %-3s\n", i++, title, tsk.get(1), tsk.get(2), tsk.get(3), tsk.get(4)); }
        System.out.println();
    }

    public static String Remove(ArrayList<ArrayList<String>> tasks, int tsk) {
        if (tasks.isEmpty() || tsk < 0 || tsk >= tasks.size())
            return "Task not found. Use: TaskToolCmd remove <number>";
        tasks.remove(tsk);
        Task.Show(tasks);
        return "Task removed.";
    }

    public static String New(ArrayList<ArrayList<String>> tasks) {
        // title, due, repeat, label, done, notes
        String[] newtask = {"","","","","",""};
        System.out.println("\nADD NEW TASK:");
        newtask[0] = Input.InputStr("Task title: ", 30);
        if (newtask[0].isBlank()) return "Nothing added. Task must have a title!";
        newtask[1] = Input.InputDate("Due (yyyy-mm-dd): ");
        if (!newtask[1].isBlank()) {        // no date = no repeat
            newtask[2] = Input.InputStr("Repeat Day/Week/Month: ", 10);
            if (newtask[2].startsWith("d")) newtask[2] = "daily";
            if (newtask[2].startsWith("w")) newtask[2] = "weekly";
            if (newtask[2].startsWith("m")) newtask[2] = "monthly"; }
        newtask[3] = Input.InputStr("Label: ", 12);
        newtask[4] = "no";
        newtask[5] = Input.InputStr("Notes: ",200);
        tasks.add(new ArrayList<>());
        for (String tsk : newtask)
            tasks.getLast().add(tsk);
        Task.Show(tasks);
        return "Task added OK";
    }

    public static String Edit(ArrayList<ArrayList<String>> tasks, int task) {
        System.out.println("EDIT TASK:");
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) return "Task not found. Use: TaskToolCmd edit <number>";
        String[] fields = {"Title", "Due", "Repeat", "Label", "Done", "Notes"};
        for (int i = 0; i < tasks.getFirst().size(); ++i)
            System.out.println(fields[i] + ": " + tasks.get(task).get(i));
        String inp = Input.InputStr("\nEdit which item? ", 8).toLowerCase();
        if (inp.equals("title")) {
            inp = Input.InputStr("Title: ", 30);
            if (inp.isBlank()) return "Not changed. Task must have a title.";
            tasks.get(task).set(0, inp); }
        if (inp.equals("due"))
            tasks.get(task).set(1, Input.InputDate("Due date (yyyy-mm-dd): "));
        if (inp.equals("repeat")) {
            inp = Input.InputStr("Repeat: daily, weekly, monthly: ", 9).toLowerCase();
            if (inp.startsWith("d")) tasks.get(task).set(2, "daily");
            if (inp.startsWith("w")) tasks.get(task).set(2, "weekly");
            if (inp.startsWith("m")) tasks.get(task).set(2, "monthly"); }
        if (inp.equals("label"))
            tasks.get(task).set(3, Input.InputStr("Label: ", 12));
        if (inp.equals("done")) {
            inp = Input.InputStr("Is task done (y/n)? ", 3).toLowerCase();
            inp = (inp.startsWith("y")) ? "yes" : "no";
            tasks.get(task).set(4, inp); }
        if (inp.equals("notes"))
            tasks.get(task).set(5, Input.InputStr("Notes: ", 200));
        Task.Show(tasks);
        return " ";
    }

    public static void View(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) {
            System.out.println("Task not found. Use: TaskToolCmd view <number>");
            return; }
        System.out.println("VIEW TASK DETAILS");
        String[] fields = {"Title: ", "Due: ", "Repeat: ", "Label: ", "Done: ", "Notes: "};
        for (int i = 0; i < tasks.get(task).size(); ++i)
            System.out.println(fields[i] + tasks.get(task).get(i));
        System.out.println();
    }

    public static String Done(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) return "Task not found. Use: TaskToolCmd done <number>";
        tasks.get(task).set(4, "yes");      // task is done
        if (tasks.get(task).get(2).isBlank()) return "Task status updated";     // no repeat
        // set next due date for repeated tasks
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        LocalDate currentDate = LocalDate.now();
        LocalDate duedate = LocalDate.parse(tasks.get(task).get(1), formatter);
        do {    // skip missed past repeat dates, set next due date in future
            if (tasks.get(task).get(2).startsWith("d")) {
                duedate = duedate.plusDays(1);
            } else if (tasks.get(task).get(2).startsWith("w")) {
                duedate = duedate.plusWeeks(1);
            } else {
                duedate = duedate.plusMonths(1);
            }
        } while (currentDate.isAfter(duedate));
        tasks.get(task).set(1, duedate.toString());     // set next due date
        tasks.get(task).set(4, "no");                   // set not done
        Task.Show(tasks);
        return "Task updated for repeat task.\nNew due date set, not done set.";
    }

    public static void Overdue(ArrayList<ArrayList<String>> tasks) {
        // list tasks due today or overdue
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        LocalDate currentDate = LocalDate.now();
        String overdueTasks = "";
        String todayTasks = "";
        for (ArrayList<String> tsk : tasks) {
            if (tsk.get(1).isBlank() || tsk.get(4).equals("yes")) continue;  // no due date or done
            LocalDate tskdue = LocalDate.parse(tsk.get(1), formatter);
            if (currentDate.isAfter(tskdue))        // it's after due date?
                overdueTasks += String.format("Task: %-18s Due: %-10s\n", tsk.get(0), tsk.get(1));
            if (currentDate.equals(tskdue))         // due date is today?
                todayTasks += String.format("Task: %-18s Due: %-10s\n", tsk.get(0), tsk.get(1));
        }
        if (!todayTasks.isEmpty())
            System.out.println("You have tasks due today:\n" + todayTasks);
        if (!overdueTasks.isEmpty())
            System.out.println("You have tasks that are overdue!\n" + overdueTasks);
    }

    public static String Sort(ArrayList<ArrayList<String>> tasks, String cmd) {
        if (tasks.size() < 3) return "Not enough tasks to sort.";
        int index = 9999;
        if (cmd.equalsIgnoreCase("title")) index = 0;
        if (cmd.equalsIgnoreCase("due")) index = 1;
        if (cmd.equalsIgnoreCase("label")) index = 3;
        if (cmd.equalsIgnoreCase("done")) index = 4;
        if (index != 0 && index != 1 && index != 3 && index != 4) return "Can't sort on that!";
        for (int i = 0; i < tasks.size()-1; ++i)
            for (int j = tasks.size()-2; j >= i; --j)
                if (tasks.get(j).get(index).compareToIgnoreCase(tasks.get(j+1).get(index)) > 0)
                    Collections.swap(tasks, j, j+1);
        Task.Show(tasks);
        return "Sorted.";
    }

    public static void Help() {
        System.out.println("COMMANDS: new/edit/done/remove/view/sort");
        System.out.println("Edit/done/remove/view require a task number");
        System.out.println("Eg. 'TaskToolCmd edit 3' to edit task 3");
        System.out.println("Sort requires field name");
        System.out.println("Eg. 'TaskToolCmd sort due' to sort by due date");
    }
}


public class TaskToolCmd {
    public static void main(String[] args) {
        ArrayList<ArrayList<String>> tasks = new ArrayList<>();     // title, due, repeat, label, done, notes
        File mac = new File("/users/shared");
        String filepath = mac.exists() ? "/users/shared/TaskTool.txt" : "/users/public/TaskTool.txt";
        FileOp.Read(filepath, tasks);
        System.out.println("----------------------------------------");
        System.out.println("           T A S K  T O O L");
        System.out.println("----------------------------------------");
        Task.Show(tasks);
        Task.Overdue(tasks);
        if (args.length ==0) Task.Show(tasks);
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("new")) System.out.println(Task.New(tasks));
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) Task.Help(); }
        if (args.length == 2) {
            int item = Input.isNumber(args[1]) ? Integer.parseInt(args[1]) : 9999;  // an invalid number
            if (args[0].equalsIgnoreCase("edit")) System.out.println(Task.Edit(tasks, item));
            if (args[0].equalsIgnoreCase("done")) System.out.println(Task.Done(tasks, item));
            if (args[0].equalsIgnoreCase("remove")) System.out.println(Task.Remove(tasks, item));
            if (args[0].equalsIgnoreCase("view")) Task.View(tasks, item);
            if (args[0].equalsIgnoreCase("sort")) System.out.println(Task.Sort(tasks, args[1])); }
        FileOp.Write(filepath, tasks);
    }
}
