// Task Tool - a text-based command line task/to-do manager
// By Roland Waddilove (github.com/rwaddilove/) as an exercise while
// learning Java. Public Domain. Use at your own risk! This is the
// command line version, eg. 'java TaskToolCmd new' to add a new task.

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

    public static void Remove(ArrayList<ArrayList<String>> tasks, int tsk) {
        if (tasks.isEmpty() || tsk < 0 || tsk >= tasks.size()) {
            System.out.println("REMOVE TASK: Task not found.");
            return; }
        System.out.println("REMOVE TASK: " + tasks.get(tsk).getFirst());
        tasks.remove(tsk);
        Task.Show(tasks);
    }

    public static void New(ArrayList<ArrayList<String>> tasks) {
        // title, due, repeat, label, done, notes
        String[] newTask = {"","","","","",""};     // build the new task here
        newTask[0] = Input.InputStr("\nADD NEW TASK\nTask title: ", 30);
        if (newTask[0].isBlank()) return;   // Nothing added. Task must have a title
        newTask[1] = Input.InputDate("Due (yyyy-mm-dd): ");
        if (!newTask[1].isBlank()) {        // only ask for repeat if there's a due date
            newTask[2] = Input.InputStr("Repeat Day/Week/Month: ", 10);
            if (newTask[2].startsWith("d")) newTask[2] = "daily";
            if (newTask[2].startsWith("w")) newTask[2] = "weekly";
            if (newTask[2].startsWith("m")) newTask[2] = "monthly"; }
        newTask[3] = Input.InputStr("Label: ", 12);
        newTask[4] = "no";
        newTask[5] = Input.InputStr("Notes: ",200);
        tasks.add(new ArrayList<>());       // add task to list and add items
        for (String item : newTask)
            tasks.getLast().add(item);
        Task.Show(tasks);
    }

    public static void Edit(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) {
            System.out.println("EDIT TASK: Task not found.");
            return; }
        String[] fields = {"Title", "Due", "Repeat", "Label", "Done", "Notes"};
        for (int i = 0; i < fields.length; ++i)
            System.out.println(fields[i] + ": " + tasks.get(task).get(i));
        String inp = Input.InputStr("\nEdit which item? ", 8).toLowerCase();
        if (inp.equals("title")) {
            inp = Input.InputStr("Title: ", 30);
            if (inp.isBlank()) return;
            tasks.get(task).set(0, inp); }
        else if (inp.equals("due"))
            tasks.get(task).set(1, Input.InputDate("Due date (yyyy-mm-dd): "));
        else if (inp.equals("repeat")) {
            inp = Input.InputStr("Repeat: daily, weekly, monthly: ", 9).toLowerCase();
            if (inp.startsWith("d")) tasks.get(task).set(2, "daily");
            if (inp.startsWith("w")) tasks.get(task).set(2, "weekly");
            if (inp.startsWith("m")) tasks.get(task).set(2, "monthly"); }
        else if (inp.equals("label"))
            tasks.get(task).set(3, Input.InputStr("Label: ", 12));
        else if (inp.equals("done")) {
            inp = Input.InputStr("Is task done (y/n)? ", 3).toLowerCase();
            inp = (inp.startsWith("y")) ? "yes" : "no";
            tasks.get(task).set(4, inp); }
        else if (inp.equals("notes"))
            tasks.get(task).set(5, Input.InputStr("Notes: ", 200));
        else {
            System.out.println("EDIT TASK: Item name not recognised."); }
        Task.Show(tasks);
    }

    public static void View(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) {
            System.out.println("VIEW TASK: Task not found.");
            return; }
        System.out.println("VIEW TASK:");
        String[] fields = {"Title", "Due", "Repeat", "Label", "Done", "Notes"};
        for (int i = 0; i < fields.length; ++i)
            System.out.println(fields[i] + ": " + tasks.get(task).get(i));
        System.out.println();
    }

    public static void Done(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) {
            System.out.println("SET TASK DONE: Task not found.");
            return; }
        tasks.get(task).set(4, "yes");      // task is done
        if (tasks.get(task).get(2).isBlank()) {
            System.out.println("Task (" + task + ") status updated");     // no repeat
            return; }
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
        System.out.println("Task (" + task + ") updated. Repeat task!\nNew due date set, not done set.");
        Task.Show(tasks);
    }

    public static void Overdue(ArrayList<ArrayList<String>> tasks) {
        String overdueTasks = "";   // list of tasks overdue
        String todayTasks = "";     // list of tasks due today
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        LocalDate currentDate = LocalDate.now();
        for (ArrayList<String> task : tasks) {
            if (task.get(1).isBlank() || task.get(4).equals("yes")) continue;  // no due date or done
            LocalDate taskdue = LocalDate.parse(task.get(1), formatter);
            if (currentDate.isAfter(taskdue))        // it's after due date?
                overdueTasks += String.format("Task: %-18s Due: %-10s\n", task.get(0), task.get(1));
            if (currentDate.equals(taskdue))         // due date is today?
                todayTasks += String.format("Task: %-18s Due: %-10s\n", task.get(0), task.get(1));
        }
        if (!todayTasks.isEmpty())
            System.out.println("You have tasks due today:\n" + todayTasks);
        if (!overdueTasks.isEmpty())
            System.out.println("You have tasks that are overdue!\n" + overdueTasks);
    }

    public static void Sort(ArrayList<ArrayList<String>> tasks, String cmd) {
        if (tasks.size() < 3) {
            System.out.println("Not enough tasks to sort.");
            return; }
        int index = 9999;
        if (cmd.equalsIgnoreCase("title")) index = 0;
        if (cmd.equalsIgnoreCase("due")) index = 1;
        if (cmd.equalsIgnoreCase("label")) index = 3;
        if (cmd.equalsIgnoreCase("done")) index = 4;
        if (index != 0 && index != 1 && index != 3 && index != 4) {
            System.out.println("Can't sort on that!");
            return; }
        for (int i = 0; i < tasks.size()-1; ++i)
            for (int j = tasks.size()-2; j >= i; --j)
                if (tasks.get(j).get(index).compareToIgnoreCase(tasks.get(j+1).get(index)) > 0)
                    Collections.swap(tasks, j, j+1);
        Task.Show(tasks);
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
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("new")) Task.New(tasks);
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) Task.Help(); }
        if (args.length == 2) {
            int item = Input.isNumber(args[1]) ? Integer.parseInt(args[1]) : 9999;  // an invalid number
            if (args[0].equalsIgnoreCase("edit")) Task.Edit(tasks, item);
            if (args[0].equalsIgnoreCase("done")) Task.Done(tasks, item);
            if (args[0].equalsIgnoreCase("remove")) Task.Remove(tasks, item);
            if (args[0].equalsIgnoreCase("view")) Task.View(tasks, item);
            if (args[0].equalsIgnoreCase("sort")) Task.Sort(tasks, args[1]); }
        FileOp.Write(filepath, tasks);
    }
}
